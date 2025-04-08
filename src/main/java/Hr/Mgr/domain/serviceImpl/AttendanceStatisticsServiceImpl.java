package Hr.Mgr.domain.serviceImpl;

import Hr.Mgr.domain.dto.AttendanceResDto;
import Hr.Mgr.domain.entity.Employee;
import Hr.Mgr.domain.entity.QuarterlyAttendanceStatistics;
import Hr.Mgr.domain.init.DataInitializer;
import Hr.Mgr.domain.repository.QuarterlyAttendanceStatisticsRepository;
import Hr.Mgr.domain.service.AttendanceService;
import Hr.Mgr.domain.service.AttendanceStatisticsService;
import Hr.Mgr.domain.service.EmployeeService;
import Hr.Mgr.domain.statistics.EmployeeQuarterlyStatAccumulator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
// Qualifier 사용을 위해 lombok @RequiredArgsConstructor 기능을 off
public class AttendanceStatisticsServiceImpl implements AttendanceStatisticsService {

    private final AttendanceService attendanceService;
    private final EmployeeService employeeService;
    private final QuarterlyAttendanceStatisticsRepository statsRepository;
    private final KafkaTemplate<String, Object> compressedKafkaTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(AttendanceStatisticsService.class);
    @Value("${custom.kafka.topic.insert-attendance-statistics}")
    private String insertAttendanceStatisticsTopic;
    @Value("${custom.kafka.topic.calculate-attendance-statistics}")
    private String calculateAttendanceStatisticsTopic;

    @Value("${server-name}")
    private String serverName;
    private Map<String, DataInitializer.DepartmentPolicy> policies = new HashMap<>();

    public AttendanceStatisticsServiceImpl(AttendanceService attendanceService, EmployeeService employeeService, QuarterlyAttendanceStatisticsRepository statsRepository, @Qualifier("compressedKafkaTemplate") KafkaTemplate<String, Object> compressedKafkaTemplate, KafkaTemplate<String, Object> kafkaTemplate, NamedParameterJdbcTemplate jdbcTemplate) {
        this.attendanceService = attendanceService;
        this.employeeService = employeeService;
        this.statsRepository = statsRepository;
        this.compressedKafkaTemplate = compressedKafkaTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.jdbcTemplate = jdbcTemplate;
    }

    private record StatKey(int year, int quarter) {}
    public record IntRange(int start, int end) {}
    @PostConstruct
    private void init() {
        writeDepartmentPolicies();
    }

    @Override
    @Transactional
    public void createAttendanceStatistics() {
        int minAttendanceYear = 2003; //attendanceService.getMinAttendanceYear();
        int maxAttendanceYear = 2012; // attendanceService.getMaxAttendanceYear();

        if(minAttendanceYear == 0 || maxAttendanceYear == 0) // 데이터 없을떄
            throw new RuntimeException("there is no attendance Year Data");

        int rangeCount = 6;
        int totalYears = maxAttendanceYear - minAttendanceYear + 1;
        int unit = totalYears / rangeCount;
        int remainder = totalYears % rangeCount;
        int start = minAttendanceYear;

        for (int i = 0; i < rangeCount; i++) {
            int end = start + unit - 1;
            if (remainder > 0) {
                end += 1;
                remainder--;
            }
            logger.info("{} : {} and {} send",serverName, start , end);
            kafkaTemplate.send(calculateAttendanceStatisticsTopic, null, new IntRange(start, end));

            start = end + 1;
            if (start > maxAttendanceYear) break;
        }
    }

    @KafkaListener(topics = "${custom.kafka.topic.calculate-attendance-statistics}", groupId = "${custom.kafka.group-id.calculate-attendance-statistics}",  containerFactory = "calculateAttendanceStatisticsKafkaListenerContainerFactory", concurrency = "3")
    public void calculateAttendanceStatistics(IntRange yearRange) {
        logger.info("{} :  calculate Attendance Statistics between {} and {}", serverName, yearRange.start, yearRange.end);
        int minAttendanceYear = yearRange.start;
        int maxAttendanceYear = yearRange.end;

        int maxAttendanceMonth =  attendanceService.getMaxAttendanceMonth(maxAttendanceYear);

        int [][] quarters = {
                {1, 3},
                {4, 6},
                {7, 9},
                {10, 12}
        };
        Map<StatKey, Map<Long, EmployeeQuarterlyStatAccumulator>> accumulators = new ConcurrentHashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<Void>> futures = new ArrayList<>();
        // 년 단위
        for (int curYear = minAttendanceYear; curYear <= maxAttendanceYear; curYear++){
            for (int[] quarter : quarters) {

                int sMonth = quarter[0];
                int eMonth = quarter[1];



                if( curYear == maxAttendanceYear){
                    if(( sMonth <= maxAttendanceMonth && eMonth >= maxAttendanceMonth))
                        eMonth = maxAttendanceMonth;
                    else if(sMonth > maxAttendanceMonth)
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
                    Page<AttendanceResDto> pageResult;
                    do {
                        Pageable pageable = PageRequest.of(page, size);
                        pageResult = attendanceService.findAttendanceDtosByYearAndMonths(finalYear, finalSMonth, finalEMonth, pageable);
                        accumulatePageData(pageResult.getContent(), empMap, finalYear, finalQuarterIndex);
                        page++;

                    }while( page <= 1 );// pageResult.hasNext());

                    logger.info(statKey + "삽입 시작");
                    finalizeAndSave(accumulators.get(statKey));
                    logger.info(statKey + "삽입 끝");
                    accumulators.remove(statKey);
                    return null;
                });

            }
        }
        //
//         snappy 압축률 계산
//        ObjectMapper objectMapper = new ObjectMapper();
//        String json = null; // data = List<MyClass> 등
//        try {
//            byte[] raw = objectMapper.writeValueAsBytes(list);
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            OutputStream out = new SnappyOutputStream(baos);
//            out.write(raw);
//            out.close();
//            byte[] byteArray = baos.toByteArray();
//
//            System.out.println("원본 크기: " + raw.length);
//            System.out.println("Snappy 압축 크기: " + byteArray.length);
//            System.out.println("압축률: " + ((double) byteArray.length / raw.length));
//
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }


    public void finalizeAndSave(Map<Long, EmployeeQuarterlyStatAccumulator> accumulators) {
        List<QuarterlyAttendanceStatistics> results = accumulators.values().stream()
                .map(EmployeeQuarterlyStatAccumulator::toFinalStatistics)
                .toList();

        compressedKafkaTemplate.send(insertAttendanceStatisticsTopic, results);

//        statsRepository.saveAll(results);
    }

    @Transactional
    @KafkaListener(topics = "${custom.kafka.topic.insert-attendance-statistics}", groupId = "${custom.kafka.group-id.insert-attendance-statistics}",  containerFactory = "insertAttendanceStatisticsKafkaListenerContainerFactory")
    public void createBatchAttendanceStatistics(List<QuarterlyAttendanceStatistics> attendanceStatisticsReqDtos) {

        try {
            List<MapSqlParameterSource> batchParams = new ArrayList<>();

            for (int batchedSize = 0; batchedSize < attendanceStatisticsReqDtos.size(); batchedSize++) {
                QuarterlyAttendanceStatistics attendanceReqDto = attendanceStatisticsReqDtos.get(batchedSize);
                String json = objectMapper.writeValueAsString(attendanceReqDto.getWeeklyWorkMinutes());
                LocalDateTime now = LocalDateTime.now();
                batchParams.add(new MapSqlParameterSource()
                        .addValue("avg_end_time", attendanceReqDto.getAvgEndTime())
                        .addValue("avg_overtime_minutes", attendanceReqDto.getAvgOvertimeMinutes())
                        .addValue("avg_start_time", attendanceReqDto.getAvgStartTime())
                        .addValue("avg_work_minutes", attendanceReqDto.getAvgWorkMinutes())
                        .addValue("department", attendanceReqDto.getDepartment())
                        .addValue("employee_id", attendanceReqDto.getEmployeeId())
                        .addValue("holiday_work_ratio", attendanceReqDto.getHolidayWorkRatio())
                        .addValue("late_count", attendanceReqDto.getLateCount())
                        .addValue("present_days", attendanceReqDto.getPresentDays())
                        .addValue("quarter", attendanceReqDto.getQuarter())
                        .addValue("present_days", attendanceReqDto.getPresentDays())
                        .addValue("weekly_work_minutes", json)
                        .addValue("year", attendanceReqDto.getYear())
                        .addValue("created_at", now)
                        .addValue("updated_at", now));

                if(batchedSize != 0 && batchedSize % 2000 == 0){
                    batchInsertAttendancesStatistics(batchParams);
                    batchParams.clear();
                }
            }
            if(!batchParams.isEmpty())
                batchInsertAttendancesStatistics(batchParams);
        }
        catch (Exception e){
            logger.warn("Kafka 에러입니다, message : {}", e);
        }
    }

    private void batchInsertAttendancesStatistics(List<MapSqlParameterSource> batchParams) {

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

    public void accumulatePageData(List<AttendanceResDto> pageData,
                                   Map<Long, EmployeeQuarterlyStatAccumulator> employeeMap,
                                   int year, int quarter) {
        try {
            Map<Long, List<AttendanceResDto>> byEmployee = pageData.stream()
                    .collect(Collectors.groupingBy(AttendanceResDto::getEmployeeId));

            for (Map.Entry<Long, List<AttendanceResDto>> entry : byEmployee.entrySet()) {
                Long employeeId = entry.getKey();
                List<AttendanceResDto> data = entry.getValue();

                Employee employee = employeeService.findEmployeeEntityById(employeeId);
                String departmentName ;
                if(employee.getDepartment() == null)
                    departmentName = "Nope";
                else departmentName = employee.getDepartment().getName();
                DataInitializer.DepartmentPolicy policy = policies.get(departmentName);

                EmployeeQuarterlyStatAccumulator acc = employeeMap.computeIfAbsent(
                        employeeId,
                        id -> new EmployeeQuarterlyStatAccumulator(employeeId, departmentName, year, quarter)
                );

                acc.accumulate(data, policy);
            }
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
