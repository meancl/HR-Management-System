package Hr.Mgr.domain.serviceImpl;

import Hr.Mgr.domain.dto.AttendanceReqDto;
import Hr.Mgr.domain.dto.AttendanceResDto;
import Hr.Mgr.domain.entity.Attendance;
import Hr.Mgr.domain.entity.Employee;
import Hr.Mgr.domain.repository.AttendanceRepository;
import Hr.Mgr.domain.service.AttendanceService;
import Hr.Mgr.domain.service.EmployeeService;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final RedisTemplate<String, String> redisTemplate;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final Logger logger = LoggerFactory.getLogger(AttendanceService.class);
    private Boolean isBatchModeActive = false;

    private static final String ATTENDANCE_LOCK_KEY = "attendance create lock";
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

//    private final String attendanceKey = "attendance-key";
    @Value("${custom.kafka.topic.attendance}")
    private String attendanceTopic;
    @Value("${custom.kafka.group-id.attendance}")
    private String attendanceGroupId;
    @Value("${spring.kafka.bootstrap-servers}")
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
                kafkaTemplate.send(attendanceTopic, dto);  // key로 partition 고정
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
                    logger.info("batch mode 수행");
                    isBatchModeActive = true;
                    scheduler.schedule(this::checkRemainingRecords, BATCH_DELAY, TimeUnit.SECONDS);
                } else if (durationSeconds > ACCESS_INTERVAL_SECONDS)
                    batchFrequencyCounter = 0;
            }
        }
    }

    private void checkRemainingRecords() {
        logger.info("Batch 연기 여부 확인");

        Properties consumerProps = new Properties();
        consumerProps.put("bootstrap.servers", kafkaServer);
        consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("group.id", attendanceGroupId);
        consumerProps.put("auto.offset.reset", "latest");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {
            // 1. 토픽의 모든 파티션 가져오기
            List<PartitionInfo> partitions = consumer.partitionsFor(attendanceTopic);
            List<TopicPartition> topicPartitions = partitions.stream()
                    .map(p -> new TopicPartition(attendanceTopic, p.partition()))
                    .toList();

            // 2. 파티션 할당
            consumer.assign(topicPartitions);

            // 3. 파티션별 offset 상태 확인
            Map<TopicPartition, Long> endOffsets = consumer.endOffsets(topicPartitions);
            Map<TopicPartition, OffsetAndMetadata> committedOffsets = new HashMap<>();
            for (TopicPartition tp : topicPartitions) {
                OffsetAndMetadata committed = consumer.committed(tp);
                committedOffsets.put(tp, committed != null ? committed : new OffsetAndMetadata(0));
            }

            boolean hasPendingMessages = false;
            for (TopicPartition tp : topicPartitions) {
                long endOffset = endOffsets.getOrDefault(tp, 0L);
                long committedOffset = committedOffsets.get(tp).offset();
                if (committedOffset < endOffset) {
                    hasPendingMessages = true;
                    break;
                }
            }

            // 4. 배치 연장 여부 결정
            if (hasPendingMessages) {
                logger.info("Batch mode 연장");
                scheduler.schedule(this::checkRemainingRecords, BATCH_DELAY, TimeUnit.SECONDS);
            } else {
                logger.info("Batch mode 종료");
                isBatchModeActive = false;
                batchFrequencyCounter = 0;
            }
        }
    }

    private Integer attendanceReqAccumSize = 0;
    @KafkaListener(topics = "${custom.kafka.topic.attendance}", groupId = "${custom.kafka.group-id.attendance}",  containerFactory = "attendanceBatchKafkaListenerContainerFactory")
    public void createBatchAttendances(List<AttendanceReqDto> attendanceReqDtos, Acknowledgment acknowledgment) {

        try {
            logger.info("listen to batch insert: {}", attendanceReqDtos.size());
            attendanceReqAccumSize += attendanceReqDtos.size();
            logger.info("batch accum size : {}", attendanceReqAccumSize);

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
            logger.warn("Kafka 에러입니다, message : {}", e);
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
    public Page<AttendanceResDto> findAttendanceDtosByYearAndMonths(Integer year, Integer startMonth, Integer endMonth, Pageable pageable) {
        return attendanceRepository.findByYearAndMonths(year, startMonth, endMonth, pageable)
                .map(AttendanceResDto::new);
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

        // checkRemainingRecords();
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new IllegalArgumentException("Attendance not found"));
        attendanceRepository.delete(attendance);
    }

    @Override
    public int getMinAttendanceYear() {
        return Optional.ofNullable(attendanceRepository.findMinYear()).orElse(0);
    }

    @Override
    public int getMaxAttendanceYear() {
        return Optional.ofNullable(attendanceRepository.findMaxYear()).orElse(0);
    }

    @Override
    public int getMaxAttendanceMonth(int year) {
        return Optional.ofNullable(attendanceRepository.findMaxMonth(year)).orElse(0);
    }
}
