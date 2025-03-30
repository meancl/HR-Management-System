package Hr.Mgr.domain.serviceImpl;

import Hr.Mgr.domain.dto.AttendanceResDto;
import Hr.Mgr.domain.entity.Employee;
import Hr.Mgr.domain.entity.QuarterlyAttendanceStatistics;
import Hr.Mgr.domain.enums.AttendanceStatus;
import Hr.Mgr.domain.enums.VacationType;
import Hr.Mgr.domain.init.DataInitializer;
import Hr.Mgr.domain.repository.QuarterlyAttendanceStatisticsRepository;
import Hr.Mgr.domain.service.AttendanceService;
import Hr.Mgr.domain.service.AttendanceStatisticsService;
import Hr.Mgr.domain.service.EmployeeService;
import Hr.Mgr.domain.statistics.EmployeeQuarterlyStatAccumulator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceStatisticsServiceImpl implements AttendanceStatisticsService {

    private final AttendanceService attendanceService;
    private final EmployeeService employeeService;
    private final QuarterlyAttendanceStatisticsRepository statsRepository;
    private Map<String, DataInitializer.DepartmentPolicy> policies = new HashMap<>();
    private record StatKey(int year, int quarter) {}

    @PostConstruct
    private void init() {
        writeDepartmentPolicies();
    }

    @Override
    @Transactional
    public void calculateAndInsertStatistics() {

        int minAttendanceYear = 2010; //attendanceService.getMinAttendanceYear();
        int maxAttendanceYear = 2010; // attendanceService.getMaxAttendanceYear();

        if(minAttendanceYear == 0 || maxAttendanceYear == 0) // 데이터 없을떄
            return;

        int maxAttendanceMonth =  3; // attendanceService.getMaxAttendanceMonth(maxAttendanceYear);

        int [][] quarters = {
                {1, 3},
                {4, 6},
                {7, 9},
                {10, 12}
        };
        Map<StatKey, Map<Long, EmployeeQuarterlyStatAccumulator>> accumulators = new HashMap<>();

        // 년 단위
        for (int curYear = minAttendanceYear; curYear <= maxAttendanceYear; curYear++){
            for (int[] quarter : quarters) {

                int sMonth = quarter[0];
                int eMonth = quarter[1];

                int page = 0;
                int size = 5000;
                Page<AttendanceResDto> pageResult;

                if( curYear == maxAttendanceYear ){
                    if(( sMonth <= maxAttendanceMonth && eMonth >= maxAttendanceMonth))
                        eMonth = maxAttendanceMonth;
                    else if(sMonth > maxAttendanceMonth)
                        continue;
                }

                int quarterIndex = (sMonth - 1) / 3 + 1;
                StatKey statKey = new StatKey(curYear, quarterIndex);
                Map<Long, EmployeeQuarterlyStatAccumulator> empMap = new HashMap<>();
                accumulators.put(statKey, empMap);

                do {
                    Pageable pageable = PageRequest.of(page, size);
                    pageResult = attendanceService.findAttendanceDtosByYearAndMonths(curYear, sMonth, eMonth, pageable);
                    accumulatePageData(pageResult.getContent(), empMap, curYear, quarterIndex);
                    page++;

                }while( page <= 4 );// pageResult.hasNext());

                finalizeAndSave(accumulators);
            }
        }
    }
    public void finalizeAndSave(Map<StatKey, Map<Long, EmployeeQuarterlyStatAccumulator>> accumulators) {
        List<QuarterlyAttendanceStatistics> results = accumulators.values().stream()
                .flatMap(empMap -> empMap.values().stream())
                .map(EmployeeQuarterlyStatAccumulator::toFinalStatistics)
                .toList();

        statsRepository.saveAll(results);
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
            System.out.println(e);

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
