package Hr.Mgr.domain.init;

import Hr.Mgr.domain.entity.Attendance;
import Hr.Mgr.domain.entity.Department;
import Hr.Mgr.domain.entity.Employee;
import Hr.Mgr.domain.enums.AttendanceStatus;
import Hr.Mgr.domain.repository.AttendanceRepository;
import Hr.Mgr.domain.repository.DepartmentRepository;
import Hr.Mgr.domain.repository.EmployeeRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;

@Component
@ConditionalOnProperty(name = "spring.jpa.hibernate.ddl-auto", havingValue = "create") // ✅ create일 때만 실행
public class DataInitializer {

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @PostConstruct
    @Transactional
    public void init() {
        Random random = new Random();

        Department hr = departmentRepository.save(new Department("HR"));
        Department finance = departmentRepository.save(new Department("Finance"));
        Department engineering = departmentRepository.save(new Department("Engineering"));
        Department sales = departmentRepository.save(new Department("Sales"));
        Department marketing = departmentRepository.save(new Department("Marketing"));
        Department support = departmentRepository.save(new Department("Customer Support"));
        Department legal = departmentRepository.save(new Department("Legal"));
        Department operations = departmentRepository.save(new Department("Operations"));
        Department rnd = departmentRepository.save(new Department("Research & Development"));
        Department it = departmentRepository.save(new Department("IT"));

        // 기본 데이터 삽입
        List<Employee> employees = List.of(
                new Employee("홍길동", "hong@company.com", bCryptPasswordEncoder.encode("password123"), 30, hr),
                new Employee("김철수", "kim@company.com", bCryptPasswordEncoder.encode("password456"), 28, sales),
                new Employee("이영희", "lee@company.com", bCryptPasswordEncoder.encode("password789"), 35, finance),
                new Employee("박지민", "park@company.com", bCryptPasswordEncoder.encode("password111"), 27, marketing),
                new Employee("최강욱", "choi@company.com", bCryptPasswordEncoder.encode("password222"), 32, hr),
                new Employee("정유진", "jung@company.com", bCryptPasswordEncoder.encode("password333"), 29, it),
                new Employee("나성민", "na@company.com", bCryptPasswordEncoder.encode("password444"), 33, rnd),
                new Employee("오승현", "oh@company.com", bCryptPasswordEncoder.encode("password555"), 31, support),
                new Employee("한지우", "han@company.com", bCryptPasswordEncoder.encode("password666"), 26, null), // ✅ 부서 없음
                new Employee("이준호", "lee.junho@company.com", bCryptPasswordEncoder.encode("password777"), 34, operations),
                new Employee("윤서영", "yoon@company.com", bCryptPasswordEncoder.encode("password888"), 27, null), // ✅ 부서 없음
                new Employee("김보람", "kim.boram@company.com", bCryptPasswordEncoder.encode("password999"), 29, finance),
                new Employee("임소연", "lim@company.com", bCryptPasswordEncoder.encode("password101"), 28, rnd),
                new Employee("전은수", "jeon@company.com", bCryptPasswordEncoder.encode("password202"), 30, hr),
                new Employee("송하나", "song@company.com", bCryptPasswordEncoder.encode("password303"), 32, it),
                new Employee("강민수", "kang@company.com", bCryptPasswordEncoder.encode("password414"), 31, null), // ✅ 부서 없음
                new Employee("서지훈", "seo@company.com", bCryptPasswordEncoder.encode("password525"), 29, sales),
                new Employee("김하늘", "kim.haneul@company.com", bCryptPasswordEncoder.encode("password636"), 27, null), // ✅ 부서 없음
                new Employee("이도윤", "lee.doyoon@company.com", bCryptPasswordEncoder.encode("password747"), 34, engineering),
                new Employee("정민지", "jung.minji@company.com", bCryptPasswordEncoder.encode("password858"), 26, marketing),
                new Employee("박은우", "park.eunwoo@company.com", bCryptPasswordEncoder.encode("password969"), 28, rnd),
                new Employee("차서윤", "cha@company.com", bCryptPasswordEncoder.encode("password070"), 30, legal),
                new Employee("한승호", "han.seungho@company.com", bCryptPasswordEncoder.encode("password181"), 35, support),
                new Employee("오연서", "oh.yeonseo@company.com", bCryptPasswordEncoder.encode("password292"), 31, hr),
                new Employee("배도윤", "bae@company.com", bCryptPasswordEncoder.encode("password303"), 33, engineering),
                new Employee("신예은", "shin.yeeun@company.com", bCryptPasswordEncoder.encode("password414"), 29, it),
                new Employee("유태양", "yoo@company.com", bCryptPasswordEncoder.encode("password525"), 27, rnd),
                new Employee("남준혁", "nam@company.com", bCryptPasswordEncoder.encode("password636"), 30, operations),
                new Employee("고은비", "go.eunbi@company.com", bCryptPasswordEncoder.encode("password747"), 26, support),
                new Employee("문지훈", "moon@company.com", bCryptPasswordEncoder.encode("password858"), 35, finance),
                new Employee("조서윤", "jo.seoyoon@company.com", bCryptPasswordEncoder.encode("password969"), 28, marketing),
                new Employee("황인성", "hwang@company.com", bCryptPasswordEncoder.encode("password070"), 32, it),
                new Employee("백예지", "baek@company.com", bCryptPasswordEncoder.encode("password181"), 27, legal),
                new Employee("손민호", "son@company.com", bCryptPasswordEncoder.encode("password292"), 31, operations),
                new Employee("엄다현", "um.dahyun@company.com", bCryptPasswordEncoder.encode("password303"), 30, null) // ✅ 부서 없음
        );

        // ✅ 한 번에 저장
        employeeRepository.saveAll(employees);

        // ✅ 최근 30일 동안 출석 데이터 생성
        for (Employee employee : employees) {
            for (int i = 0; i < 30; i++) {
                LocalDate attendanceDate = LocalDate.now().minusDays(i);

                // 출근 시간 (지각 or 정상)
                boolean isLate = random.nextDouble() < 0.2; // 20% 확률로 지각
                LocalTime checkInTime = isLate ? LocalTime.of(9, random.nextInt(30) + 30) // 9:30 ~ 9:59 지각
                        : LocalTime.of(9, random.nextInt(30)); // 9:00 ~ 9:29 정상

                // 퇴근 시간
                boolean isEarlyLeave = random.nextDouble() < 0.1; // 10% 확률로 조퇴
                LocalTime checkOutTime = isEarlyLeave ? LocalTime.of(16, random.nextInt(60)) // 16:00 ~ 16:59 조퇴
                        : LocalTime.of(18, random.nextInt(60)); // 18:00 ~ 18:59 정상 퇴근

                // 출석 상태 설정 (출근, 결근, 휴일, 지각 등)
                AttendanceStatus status;
                double chance = random.nextDouble();
                if (chance < 0.05) {
                    status = AttendanceStatus.ABSENT; // 5% 확률 결근
                    checkInTime = null;
                    checkOutTime = null;
                } else if (chance < 0.1) {
                    status = AttendanceStatus.HOLIDAY; // 5% 확률 휴일
                    checkInTime = null;
                    checkOutTime = null;
                } else if (isLate) {
                    status = AttendanceStatus.LATE; // 지각
                } else if (isEarlyLeave) {
                    status = AttendanceStatus.LEAVE; // 조퇴
                } else {
                    status = AttendanceStatus.PRESENT; // 정상 출근
                }

                // 출석 데이터 저장
                attendanceRepository.save(new Attendance(employee, attendanceDate, checkInTime, checkOutTime, status));
            }
        }


    }
}