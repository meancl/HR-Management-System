package Hr.Mgr.domain.serviceImpl;

import Hr.Mgr.domain.aspect.MeasureExecutionTime;
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
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceService.class);

    private final AttendanceRepository attendanceRepository;
    private final EmployeeService employeeService;
    private final RedisTemplate<String, String> redisTemplate;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${custom.kafka.topic.attendance}")
    private String attendanceTopic;
    @Value("${custom.kafka.group-id.attendance}")
    private String attendanceGroupId;
    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaServer;

    private static final String ATTENDANCE_LOCK_KEY = "attendance create lock";
    private AtomicBoolean isBatchModeActive = new AtomicBoolean(false);
    private Integer batchFrequencyCounter  = 0;
    private LocalDateTime batchInitTime;
    private final Integer MAX_ACCESS_COUNT = 10;
    private final Integer ACCESS_INTERVAL_SECONDS= 5;
    private final Integer BATCH_DELAY = 60;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);




    @Override
    public AttendanceResDto createAttendance(AttendanceReqDto dto) {
        // redis lock
        String lockKey = ATTENDANCE_LOCK_KEY + dto.getEmployeeId();
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "lock", Duration.ofSeconds(60* 60));
        if (!Boolean.TRUE.equals(lockAcquired)) {
            return null;
        }

        // 배치모드 판단
        Boolean isBatchMode = checkBatchModeActive();
        try {
            // 배치모드
            if (isBatchMode) {
                kafkaTemplate.send(attendanceTopic, dto);
                AttendanceResDto returnDto = new AttendanceResDto();
                returnDto.setIsProcessed(false);
                return returnDto;
            }
            // 싱글모드
            else {
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



    /*
     * 배치 모드 활성 여부를 판단하는 메서드
     *
     * - 특정 시간 내에 요청이 일정 횟수를 초과하면 배치 모드 활성화
     * - 활성화 시, 배치 모드를 유지할지 판단하는 함수 예약
     *
     * @return 현재 배치 모드 활성 상태 (true: 활성화됨, false: 비활성화됨)
     */
    private Boolean checkBatchModeActive(){
        // 빠른 리턴 (락 최소화)
        if (isBatchModeActive.get())
            return true;

        synchronized (this) {
            if(!isBatchModeActive.get()) {
                if (batchFrequencyCounter++ == 0)
                    batchInitTime = LocalDateTime.now();
                else {
                    long durationSeconds = Duration.between(batchInitTime, LocalDateTime.now()).toSeconds();
                    // 요청 빈도 체크
                    if (durationSeconds <= ACCESS_INTERVAL_SECONDS && batchFrequencyCounter >= MAX_ACCESS_COUNT) {
                        logger.info("batch mode 수행");
                        isBatchModeActive.set(true);
                        // 카프카 잔여 파티션에 따라, 배치 모드를 유지할지 판단하는 함수 예약
                        scheduler.schedule(this::checkRemainingKafkaRecords, BATCH_DELAY, TimeUnit.SECONDS);
                    } else if (durationSeconds > ACCESS_INTERVAL_SECONDS)
                        batchFrequencyCounter = 0;
                }
            }
            return isBatchModeActive.get();
        }
    }

    private void checkRemainingKafkaRecords() {
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
                scheduler.schedule(this::checkRemainingKafkaRecords, BATCH_DELAY, TimeUnit.SECONDS);
            } else {
                logger.info("Batch mode 종료");
                isBatchModeActive.set(false);
                batchFrequencyCounter = 0;
            }
        }
    }

    public void insertAttendanceBatch(List<AttendanceReqDto> attendanceReqDtos) {

        List<MapSqlParameterSource> batchParams = new ArrayList<>();
        for (AttendanceReqDto attendanceReqDto : attendanceReqDtos) {
            batchParams.add(new MapSqlParameterSource()
                    .addValue("employee_id", attendanceReqDto.getEmployeeId())
                    .addValue("attendance_date", attendanceReqDto.getAttendanceDate())
                    .addValue("check_in_time", attendanceReqDto.getCheckInTime())
                    .addValue("check_out_time", attendanceReqDto.getCheckOutTime())
                    .addValue("status", attendanceReqDto.getAttendanceStatus().toString())
                    .addValue("created_at", LocalDateTime.now())
                    .addValue("updated_at", LocalDateTime.now()));
        }
        String sql = """
                INSERT INTO attendance_partitioned (employee_id, attendance_date, check_in_time, check_out_time, status) 
                VALUES (:employee_id, :attendance_date, :check_in_time, :check_out_time, :status)
            """;
        jdbcTemplate.batchUpdate(sql, batchParams.toArray(new MapSqlParameterSource[0]));
    }


    @Override
    @Transactional(readOnly = true)
    @MeasureExecutionTime
    public Page<Attendance> findAttendancePageByYearAndMonths(Integer year, Integer startMonth, Integer endMonth, Pageable pageable) {
        LocalDate startDate = LocalDate.of(year, startMonth, 1);
        LocalDate endDate = LocalDate.of(year, endMonth, 1).plusMonths(1);
        return attendanceRepository.findPageByAttendanceDateBetween(startDate, endDate, pageable);
    }

    @Transactional(readOnly = true)
    @MeasureExecutionTime
    public Slice<Attendance> findAttendanceSliceByYearAndMonths(Long id, Integer year, Integer startMonth, Integer endMonth, Pageable pageable) {
        LocalDate startDate = LocalDate.of(year, startMonth, 1);
        LocalDate endDate = LocalDate.of(year, endMonth, 1).plusMonths(1);
        return attendanceRepository.findSliceByAttendanceDateBetweenAndIdAfter(startDate, endDate, id, pageable);
    }

    @Override
    public int getMinAttendanceYear() {
        return attendanceRepository.findMinDate().map(LocalDate::getYear).orElse(0);
    }

    @Override
    public int getMaxAttendanceYear() {
        return attendanceRepository.findMaxDate().map(LocalDate::getYear).orElse(0);
    }

    @Override
    public int getMaxAttendanceMonth(int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = start.plusYears(1);
        return attendanceRepository.findMaxMonthInYear(start, end).orElse(0);
    }

}
