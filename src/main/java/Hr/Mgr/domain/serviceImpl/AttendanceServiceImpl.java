package Hr.Mgr.domain.serviceImpl;

import Hr.Mgr.domain.dto.AttendanceReqDto;
import Hr.Mgr.domain.dto.AttendanceResDto;
import Hr.Mgr.domain.entity.Attendance;
import Hr.Mgr.domain.entity.Employee;
import Hr.Mgr.domain.repository.AttendanceRepository;
import Hr.Mgr.domain.service.AttendanceService;
import Hr.Mgr.domain.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.stream;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeService employeeService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final KafkaTemplate<String, AttendanceReqDto> kafkaTemplate;

    private Boolean isBatchModeActive = false;

    private static final String ATTENDANCE_LOCK_KEY = "attendance create lock";
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final String attendanceKey = "attendance-key";
    @Value("${spring.kafka-var.topic.attendance}")
    private String attendanceTopic ;
    @Value("${spring.kafka-var.group-id.attendance}")
    private String attendanceGroupId;
    @Value("${spring.kafka-var.server}")
    private String kafkaServer;


    @Override
    public AttendanceResDto createAttendance(AttendanceReqDto dto) {

        // redis lock
        String lockKey = ATTENDANCE_LOCK_KEY + dto.getEmployeeId();
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "lock", Duration.ofSeconds(10));
        if (lockAcquired == null || !lockAcquired) {
            return null; // 이미 락이 설정되어 있으면 생성 불가
        }

        // create Attendance
        try {
            if (isBatchModeActive) {
                kafkaTemplate.send(attendanceTopic, attendanceKey, dto);  // key로 partition 고정
                AttendanceResDto returnDto = new AttendanceResDto();
                returnDto.setIsProcessed(false);
                return returnDto;
            }
            else {
                checkBatchModeActive();
                Employee employee = employeeService.findEmployeeEntityById(dto.getEmployeeId());
                Attendance attendance = new Attendance(
                        employee, dto.getAttendanceDate(), dto.getCheckInTime(), dto.getCheckOutTime(), dto.getAttendanceStatus()
                );
                return new AttendanceResDto(attendanceRepository.save(attendance));
            }
        }

        finally {
            redisTemplate.delete(lockKey);
        }

    }

    private Integer batchFrequencyCounter  = 0;
    private LocalDateTime batchInitTime;
    private final Integer MAX_ACCESS_COUNT = 3;
    private final Integer ACCESS_INTERVAL_SECONDS= 10;
    private final Integer BATCH_DELAY = 10; // batch 작업 수행시간

    /*
    *  ACCESS_INTERVAL_SECONDS 시간 안에 MAX_ACCESS_COUNT 이상의 요청이 들어오면
    *  isBatchModeActive 활성화 및 BATCH_DELAY 시간 후 처리할 데이터 체크 메서드 예약
    * */
    private void checkBatchModeActive(){

        if(!isBatchModeActive) {
            if (batchFrequencyCounter++ == 0) // 처음 접근이라면
                batchInitTime = LocalDateTime.now();
            else {
                long durationSeconds = Duration.between(batchInitTime, LocalDateTime.now()).toSeconds();
                if (durationSeconds <= ACCESS_INTERVAL_SECONDS && batchFrequencyCounter >= MAX_ACCESS_COUNT) {
                    System.out.println("batch mode 수행");
                    isBatchModeActive = true;
                    scheduler.schedule(this::checkRemainingRecords, BATCH_DELAY, TimeUnit.SECONDS);
                } else if (durationSeconds > ACCESS_INTERVAL_SECONDS)
                    batchFrequencyCounter = 0;
            }
        }
    }

    private void checkRemainingRecords() {
        System.out.println("batch 연기 여부 확인");
        int numPartitions = 1;
        int partition = Math.abs(attendanceKey.hashCode() % numPartitions); // key에 매핑되는 파티션 가져오기
        TopicPartition topicPartition = new TopicPartition(attendanceTopic, partition);

        Properties consumerProps = new Properties();
        consumerProps.put("bootstrap.servers", kafkaServer);
        consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("group.id", attendanceGroupId);
        consumerProps.put("auto.offset.reset", "latest");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)){
            consumer.assign(Collections.singletonList(topicPartition));

            long endOffset = consumer.endOffsets(Collections.singletonList(topicPartition)).get(topicPartition);
            Map<TopicPartition, OffsetAndMetadata> committedOffsets = consumer.committed(Collections.singleton(topicPartition));
            long currentOffset = committedOffsets != null && committedOffsets.containsKey(topicPartition) ?
                    committedOffsets.get(topicPartition).offset() : 0;
            if(currentOffset < endOffset){
                System.out.println("batch mode 연장");
                scheduler.schedule(this::checkRemainingRecords, BATCH_DELAY, TimeUnit.SECONDS);
            }

            else
            {
                System.out.println("batch mode 종료");
                isBatchModeActive = false;
                batchFrequencyCounter = 0;
            }
        }
    }
    @KafkaListener(topics = "${spring.kafka-var.topic.attendance}", groupId = "${spring.kafka-var.group-id.attendance}",  containerFactory = "attendanceBatchKafkaListenerContainerFactory")
    public void createBatchAttendances(List<AttendanceReqDto> attendanceReqDtos, Acknowledgment acknowledgment) {

        try {
            System.out.println("listen to batch insert:" + attendanceReqDtos.size());

            List<MapSqlParameterSource> batchParams = new ArrayList<>();

            for (AttendanceReqDto attendanceReqDto : attendanceReqDtos) {
                batchParams.add(new MapSqlParameterSource()
                        .addValue("employee_id", attendanceReqDto.getEmployeeId())
                        .addValue("attendance_date", attendanceReqDto.getAttendanceDate())
                        .addValue("check_in_time", attendanceReqDto.getCheckInTime())
                        .addValue("check_out_time", attendanceReqDto.getCheckOutTime())
                        .addValue("status", attendanceReqDto.getAttendanceStatus().toString()));
            }

            batchInsertAttendances(batchParams);
            acknowledgment.acknowledge();
        }
        catch (Exception e){
            System.out.println("kafka 에러입니다" + e);
        }
    }



    private void batchInsertAttendances(List<MapSqlParameterSource> batchParams) {
        // TODO. attendance로 변경
        String sql = """
                INSERT INTO attendance (employee_id, attendance_date, check_in_time, check_out_time, status) 
                VALUES (:employee_id, :attendance_date, :check_in_time, :check_out_time, :status)
            """;

        jdbcTemplate.batchUpdate(sql, batchParams.toArray(new MapSqlParameterSource[0]));
    }
    @Override
    @Transactional(readOnly = true)
    public AttendanceResDto findLatestAttendanceDtoByEmployeeId(Long employeeId) {
        return attendanceRepository.findTopByEmployeeIdOrderByAttendanceDateDesc(employeeId)
                .map(AttendanceResDto::new).orElseThrow(() -> new IllegalArgumentException("No attendance found for employee"));
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResDto findAttendanceDtoById(Long attendanceId) {
        return attendanceRepository.findById(attendanceId)
                .map(AttendanceResDto::new).orElseThrow(() -> new IllegalArgumentException("No attendance found for employee"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResDto> findAttendanceDtosByEmployeeId(Long employeeId) {
        return attendanceRepository.findAllByEmployeeId(employeeId)
                .stream().map(AttendanceResDto::new).toList();
    }

    @Override
    public AttendanceResDto updateAttendance(Long attendanceId, AttendanceReqDto dto) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new IllegalArgumentException("Attendance not found"));
        if(dto.getAttendanceDate() != null) attendance.setAttendanceDate(dto.getAttendanceDate());
        if(dto.getCheckInTime() != null) attendance.setCheckInTime(dto.getCheckInTime());
        if(dto.getCheckOutTime() != null) attendance.setCheckOutTime(dto.getCheckOutTime());
        if(dto.getAttendanceStatus() != null) attendance.setStatus(dto.getAttendanceStatus());

        return new AttendanceResDto(attendanceRepository.save(attendance));
    }

    @Override
    public void deleteAttendance(Long attendanceId) {

        checkRemainingRecords();
//        Attendance attendance = attendanceRepository.findById(attendanceId)
//                .orElseThrow(() -> new IllegalArgumentException("Attendance not found"));
//        attendanceRepository.delete(attendance);
    }
}
