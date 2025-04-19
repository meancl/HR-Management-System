package Hr.Mgr.domain.serviceImpl;

import Hr.Mgr.domain.aspect.LogStartTime;
import Hr.Mgr.domain.aspect.MeasureExecutionTime;
import Hr.Mgr.domain.dto.AttendanceResDto;
import Hr.Mgr.domain.entity.Attendance;
import Hr.Mgr.domain.entity.Employee;
import Hr.Mgr.domain.entity.QuarterlyAttendanceStatistics;
import Hr.Mgr.domain.init.DataInitializer;
import Hr.Mgr.domain.repository.QuarterlyAttendanceStatisticsRepository;
import Hr.Mgr.domain.service.AttendanceService;
import Hr.Mgr.domain.service.AttendanceStatisticsService;
import Hr.Mgr.domain.service.EmployeeService;
import Hr.Mgr.domain.statistics.EmployeeQuarterlyStatAccumulator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
// Qualifier 사용을 위해 lombok @RequiredArgsConstructor 기능을 off
public class AttendanceStatisticsServiceImpl implements AttendanceStatisticsService {

    private final AttendanceService attendanceService;
    private final EmployeeService employeeService;
    private final QuarterlyAttendanceStatisticsRepository statsRepository;
    private final KafkaTemplate<String, Object> compressedKafkaTemplate;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final ExecutorService executor;
    private final ObjectMapper objectMapper;
    public static final String ATTENDANCE_STATISTICS_YEAR_KEY = "stats:year";
    private static final Logger logger = LoggerFactory.getLogger(AttendanceStatisticsService.class);
    @Value("${custom.kafka.topic.insert-attendance-statistics}")
    private String insertAttendanceStatisticsTopic;

    @Value("${server-name}")
    private String serverName;
    public static Map<String, DataInitializer.DepartmentPolicy> policies = new HashMap<>();

    public AttendanceStatisticsServiceImpl(AttendanceService attendanceService, EmployeeService employeeService, QuarterlyAttendanceStatisticsRepository statsRepository, @Qualifier("compressedKafkaTemplate") KafkaTemplate<String, Object> compressedKafkaTemplate, NamedParameterJdbcTemplate jdbcTemplate, RedisTemplate redisTemplate, ExecutorService executor, ObjectMapper objectMapper) {
        this.attendanceService = attendanceService;
        this.employeeService = employeeService;
        this.statsRepository = statsRepository;
        this.compressedKafkaTemplate = compressedKafkaTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.redisTemplate = redisTemplate;
        this.executor = executor;
        this.objectMapper = objectMapper;
    }

    private record StatKey(int year, int quarter) {}
    public record IntRange(int start, int end) {}
    @PostConstruct
    private void init() {
        writeDepartmentPolicies();
    }

    @Override
    @LogStartTime
    public void createAttendanceStatistics() {
        int minAttendanceYear = 2015; // attendanceService.getMinAttendanceYear();
        int maxAttendanceYear = 2019; // attendanceService.getMaxAttendanceYear();
 //  2020 2021 2022 2023 2024 2025
        if(minAttendanceYear == 0 || maxAttendanceYear == 0) // 데이터 없을떄
            throw new RuntimeException("there is no attendance Year Data");


        for (int start = minAttendanceYear; start <= maxAttendanceYear; start++) {
            logger.info("{} : {} send", serverName, start);
            IntRange intRange = new IntRange(start, start);
            try {
                String yearRangeJsonData = objectMapper.writeValueAsString(intRange);
                redisTemplate.opsForList().rightPush(ATTENDANCE_STATISTICS_YEAR_KEY, yearRangeJsonData);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        //        int start = minAttendanceYear;
//
//        while (start <= maxAttendanceYear) {
//            int end = Math.min(start + 1, maxAttendanceYear);  // 2년 단위로, 마지막 1년은 혼자 처리됨
//
//            logger.info("{} : {} and {} send", serverName, start, end);
//            IntRange intRange = new IntRange(start, end);
//
//            try {
//                String yearRangeJsonData = objectMapper.writeValueAsString(intRange);
//                redisTemplate.opsForList().rightPush(ATTENDANCE_STATISTICS_YEAR_KEY, yearRangeJsonData);
//            } catch (JsonProcessingException e) {
//                throw new RuntimeException(e);
//            }
//
//            start = end + 1;
//        }
        // redis alarm
        redisTemplate.convertAndSend("statistics", "start!");
    }

    @MeasureExecutionTime
    public void calculateAttendanceStatistics(IntRange yearRange) {
        logger.info("{} :  calculate Attendance Statistics between {} and {}", serverName, yearRange.start, yearRange.end);

        int minAttendanceYear = yearRange.start;
        int maxAttendanceYear = yearRange.end;

        int maxAttendanceMonth = attendanceService.getMaxAttendanceMonth(maxAttendanceYear);

        int[][] quarters = {
                {1, 3},
                {4, 6},
                {7, 9},
                {10, 12}
        };
        Map<StatKey, Map<Long, EmployeeQuarterlyStatAccumulator>> accumulators = new ConcurrentHashMap<>();

        List<Future<Void>> futures = new ArrayList<>();
        // 년 단위
        for (int curYear = minAttendanceYear; curYear <= maxAttendanceYear; curYear++) {
            for (int[] quarter : quarters) {

                int sMonth = quarter[0];
                int eMonth = quarter[1];


                if (curYear == maxAttendanceYear) {
                    if ((sMonth <= maxAttendanceMonth && eMonth >= maxAttendanceMonth))
                        eMonth = maxAttendanceMonth;
                    else if (sMonth > maxAttendanceMonth)
                        continue;
                }

                int quarterIndex = (sMonth - 1) / 3 + 1;
                StatKey statKey = new StatKey(curYear, quarterIndex);
                Map<Long, EmployeeQuarterlyStatAccumulator> empMap = new HashMap<>();
                accumulators.put(statKey, empMap);

                final int finalYear = curYear;
                final int finalQuarterIndex = quarterIndex;
                final int finalSMonth = sMonth;
                final int finalEMonth = eMonth;

                Future<Void> future = executor.submit(() -> {
                    logger.info(statKey + "작업 시작");
                    int page = 0;
                    int size = 5000;
                    Page<Attendance> pageResult;
                    do {
                        logger.info("{} on {}", statKey, page);
                        Pageable pageable = PageRequest.of(page, size);
                        pageResult = attendanceService.findAttendanceEntitiesByYearAndMonths(finalYear, finalSMonth, finalEMonth, pageable);
                        logger.info("📊 pageData size = {}", pageResult.getContent().size());
                        accumulatePageData(pageResult.getContent(), empMap, finalYear, finalQuarterIndex);
                        page++;

                    } while (page < 10); //  pageResult.hasNext());

                    logger.info(statKey + "삽입 시작");
                    sendKafkaToFinalizeStatistics(accumulators.get(statKey));
                    logger.info(statKey + "삽입 끝");
                    accumulators.remove(statKey);
                    return null;
                });

                futures.add(future);
            }
        }

        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error("❌ 통계 처리 중 오류 발생 ", e);
            }
        }
        logger.info("✅ ( {} , {} ) 모든 통계 계산 완료", minAttendanceYear, maxAttendanceYear );
    }

    public void sendKafkaToFinalizeStatistics(Map<Long, EmployeeQuarterlyStatAccumulator> accumulators) {
        List<QuarterlyAttendanceStatistics> results = accumulators.values().stream()
                .map(EmployeeQuarterlyStatAccumulator::toFinalStatistics)
                .toList();

        int BATCH_SIZE = 10;
        for (int i = 0; i < results.size(); i += BATCH_SIZE) {
            List<QuarterlyAttendanceStatistics> batch = results.subList(
                    i, Math.min(i + BATCH_SIZE, results.size())
            );
            compressedKafkaTemplate.send(insertAttendanceStatisticsTopic, batch);
        }
    }




    @Transactional
    public void batchInsertAttendancesStatistics(List<MapSqlParameterSource> batchParams) {

        String sql = "INSERT INTO quarterly_attendance_statistics ("
                + "avg_end_time, avg_overtime_minutes, avg_start_time, avg_work_minutes, "
                + "department, employee_id, holiday_work_ratio, late_count, "
                + "present_days, quarter, weekly_work_minutes, year, created_at, updated_at"
                + ") VALUES ("
                + ":avg_end_time, :avg_overtime_minutes, :avg_start_time, :avg_work_minutes, "
                + ":department, :employee_id, :holiday_work_ratio, :late_count, "
                + ":present_days, :quarter, :weekly_work_minutes, :year, :created_at, :updated_at"
                + ")";

        jdbcTemplate.batchUpdate(sql, batchParams.toArray(new MapSqlParameterSource[0]));
    }


    public void accumulatePageData(List<Attendance> pageData,
                                   Map<Long, EmployeeQuarterlyStatAccumulator> employeeMap,
                                   int year, int quarter) {
        try {
            // TODO. employee데이터가 몇개 들어오는지 확인
            // TODO. 실제 sql 문 확인
            Map<Long, List<Attendance>> byEmployee = pageData.stream()
                    .collect(Collectors.groupingBy(att -> att.getEmployee().getId()));

            long start = System.currentTimeMillis();
            int  cnt = 0 ;
            for (Map.Entry<Long, List<Attendance>> entry : byEmployee.entrySet()) {
                cnt ++;
                Long employeeId = entry.getKey();
                List<Attendance> data = entry.getValue();

                String departmentName;

                Employee employee = data.get(0).getEmployee();
                if(employee.getDepartment() == null)
                    departmentName = "Nope";
                else departmentName = employee.getDepartment().getName();


                DataInitializer.DepartmentPolicy policy = policies.get(departmentName);

                EmployeeQuarterlyStatAccumulator acc = employeeMap.computeIfAbsent(
                        employeeId,
                        id -> new EmployeeQuarterlyStatAccumulator(employeeId, departmentName, year, quarter)
                );

                acc.accumulate(data, policy);
                long end = System.currentTimeMillis();

            }
            long end = System.currentTimeMillis();
            logger.info("sxx 실행 시간: {}ms and {}cnt ", (end - start), cnt);
        }
        catch (Exception e){
            logger.error(e.toString());
        }
    }



    private void writeDepartmentPolicies() {
        policies.put("HR", new DataInitializer.DepartmentPolicy(2500000, 3500000, 3, 7, 0.3,
                LocalTime.of(9, 0), LocalTime.of(18, 0), 0.1, 30, 60));

        policies.put("Finance", new DataInitializer.DepartmentPolicy(3000000, 4500000, 5, 10, 0.4,
                LocalTime.of(8, 30), LocalTime.of(18, 30), 0.2, 60, 120));

        policies.put("Engineering", new DataInitializer.DepartmentPolicy(4000000, 7000000, 8, 15, 0.6,
                LocalTime.of(10, 0), LocalTime.of(19, 0), 0.5, 60, 180));

        policies.put("Sales", new DataInitializer.DepartmentPolicy(2800000, 4200000, 5, 12, 0.5,
                LocalTime.of(9, 0), LocalTime.of(18, 30), 0.3, 30, 90));

        policies.put("Marketing", new DataInitializer.DepartmentPolicy(2700000, 4000000, 4, 9, 0.35,
                LocalTime.of(9, 30), LocalTime.of(18, 30), 0.2, 30, 60));

        policies.put("Customer Support", new DataInitializer.DepartmentPolicy(2200000, 3000000, 3, 6, 0.25,
                LocalTime.of(8, 0), LocalTime.of(17, 0), 0.15, 30, 45));

        policies.put("Legal", new DataInitializer.DepartmentPolicy(3500000, 5500000, 6, 10, 0.4,
                LocalTime.of(9, 0), LocalTime.of(18, 0), 0.2, 60, 90));

        policies.put("Operations", new DataInitializer.DepartmentPolicy(2600000, 3800000, 4, 8, 0.3,
                LocalTime.of(8, 30), LocalTime.of(17, 30), 0.25, 45, 75));

        policies.put("Research & Development", new DataInitializer.DepartmentPolicy(3700000, 6000000, 7, 12, 0.5,
                LocalTime.of(10, 0), LocalTime.of(19, 30), 0.4, 90, 150));

        policies.put("IT", new DataInitializer.DepartmentPolicy(3200000, 5000000, 6, 11, 0.45,
                LocalTime.of(9, 0), LocalTime.of(18, 30), 0.35, 60, 120));

        policies.put("Nope", new DataInitializer.DepartmentPolicy(2600000, 3800000, 4, 8, 0.3,
                LocalTime.of(8, 30), LocalTime.of(17, 30), 0.25, 45, 75));
    }
}
