package Hr.Mgr.domain.init;

import Hr.Mgr.domain.entity.Employee;
import Hr.Mgr.domain.entity.Department;
import Hr.Mgr.domain.entity.Salary;
import Hr.Mgr.domain.enums.SalaryStatus;
import Hr.Mgr.domain.repository.EmployeeRepository;
import Hr.Mgr.domain.repository.DepartmentRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.IntStream;

@Component
public class DataInitializer implements CommandLineRunner {

    @Value("${app.giant_data_init}") // YAML 변수 가져오기
    private String dataInitMode;
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private DepartmentRepository departmentRepository;

    private static final int BATCH_SIZE = 2000; // JDBC batch 크기 설정
    private static final int CREATE_EMPLOYEE_NUMBER = 10000; // 생성할 직원 수

    @Override
    @Transactional 
    public void run(String... args) throws Exception {

        if (!"create".equalsIgnoreCase(dataInitMode)) {
            System.out.println("✅ 데이터 초기화 스킵 (app.data-init = " + dataInitMode + ")");
            return;
        }

        Random random = new Random();
//        List<MapSqlParameterSource> batchParams = new ArrayList<>();
        /*
        * 직원 데이터 생성 코드
        * */
//        // 부서 생성 및 저장
//        List<Department> departments = saveDepartments();
//        // 이메일 중복 방지를 위한 Set
//        Set<String> usedEmails = new HashSet<>();
//        // 직원 데이터 생성
//        for (int i = 0; i < CREATE_EMPLOYEE_NUMBER; i++) {
//            String name = generateRandomName();
//            String email = generateUniqueEmail(usedEmails);
//            String password = bCryptPasswordEncoder.encode("password" + i);
//            Integer age = 20 + random.nextInt(40);
//
//            // 랜덤 부서 ID 할당 (NULL 가능)
//            Long departmentId = random.nextDouble() < 0.1 ? null : departments.get(random.nextInt(departments.size())).getId();
//
//            // SQL 파라미터 바인딩
//            MapSqlParameterSource params = new MapSqlParameterSource()
//                    .addValue("name", name)
//                    .addValue("email", email)
//                    .addValue("password", password)
//                    .addValue("age", age)
//                    .addValue("department_id", departmentId);
//
//            batchParams.add(params);
//
//            // BATCH_SIZE 단위로 INSERT 실행
//            if (batchParams.size() >= BATCH_SIZE) {
//                System.out.println("done ");
//                batchInsertEmployees(batchParams);
//                batchParams.clear();
//            }
//        }
//        // 남은 데이터 처리
//        if (!batchParams.isEmpty()) {
//            batchInsertEmployees(batchParams);
//        }


        /*
        * salary 데이터 생성
        * */
        LocalDate today = LocalDate.now();

        List<MapSqlParameterSource> batchParamsSalary = new ArrayList<>();
        List<MapSqlParameterSource> batchParamsAttendance = new ArrayList<>();

        int priceUnit = 100000;
        for (int employeeNumber = 1; employeeNumber <= CREATE_EMPLOYEE_NUMBER ; employeeNumber++) {

            System.out.println(employeeNumber + "번째 salary 데이터 추가");

            int totalMonths = (random.nextInt(25 * 12 + 4 - 9) + 9); // 9개월 ~ 303개월(25년 3개월)
            // 시작 월 = 현재 날짜 - 총 근무 개월 수
            LocalDate startDate = today.minusMonths(totalMonths);

            int[] salaryOptions = IntStream.rangeClosed(25, 33).map(i -> i * priceUnit).toArray();
            BigDecimal baseSalary = new BigDecimal(salaryOptions[random.nextInt(salaryOptions.length)]);

            // salary 작업
//            for (int i = 0; i <= totalMonths; i++) {
//                LocalDate workMonth = startDate.plusMonths(i);
//                LocalDate paymentDate = workMonth.withDayOfMonth(15).plusMonths(1); // 다음달 15일 지급
//
//                // 매년 1월, 50% 확률로 10% 인상
//                if (workMonth.getMonthValue() == 1 && random.nextBoolean()) {
//                    baseSalary = baseSalary
//                            .multiply(new BigDecimal("1.10"))
//                            .divide(new BigDecimal(priceUnit), 0, RoundingMode.HALF_UP) // 10으로 나눠서 반올림
//                            .multiply(new BigDecimal(priceUnit));                        // 다시 10 곱해서 10의 배수로
//                }
//
//                // 보너스: 짝수월 + 20% 확률
//                BigDecimal bonus = BigDecimal.ZERO;
//                if (workMonth.getMonthValue() % 2 == 0 && random.nextInt(100) < 20) {
//                    bonus = new BigDecimal(500000)
//                            .add( baseSalary.multiply(new BigDecimal("0.05"))
//                                            .divide(new BigDecimal(priceUnit), 0, RoundingMode.HALF_UP) // 10으로 나눠서 반올림
//                                            .multiply(new BigDecimal(priceUnit)));
//                }
//
//                // status 계산
//                String status = today.isBefore(paymentDate) ? "PENDING" : "PAID";
//
//
//                MapSqlParameterSource params = new MapSqlParameterSource()
//                        .addValue("employeeId", employeeNumber)
//                        .addValue("amount", baseSalary)
//                        .addValue("bonus", bonus)
//                        .addValue("paymentDate", paymentDate)
//                        .addValue("status", status);
//
//                batchParamsSalary.add(params);
//
//                // BATCH_SIZE 단위로 INSERT 실행
//                if (batchParamsSalary.size() >= BATCH_SIZE) {
//                    System.out.println("done salary");
//                    batchInsertSalaries(batchParamsSalary);
//                    batchParamsSalary.clear();
//                }
//            }


            // attendance 작업
            LocalDate attendanceStart = startDate.withDayOfMonth(1);
            LocalDate attendanceEnd = today;

            // 날짜 순회 (월~토 조건 포함)
//            for (LocalDate date = attendanceStart; !date.isAfter(attendanceEnd); date = date.plusDays(1)) {
//
//                DayOfWeek dayOfWeek = date.getDayOfWeek();
//
//                boolean isWeekday = dayOfWeek != DayOfWeek.SUNDAY;
//                boolean isSaturday = dayOfWeek == DayOfWeek.SATURDAY;
//
//                boolean allowSaturday = isSaturday && random.nextInt(100) < 5;
//                if (!isWeekday || (isSaturday && !allowSaturday)) {
//                    continue; // 일요일 제외, 토요일은 5% 확률만 허용
//                }
//
//                // 출근 상태 결정
//                boolean isAbsent = random.nextInt(100) < 1; (1%로 지각)
//                String status = isAbsent ? "ABSENT" : "PRESENT";
//
//                LocalTime checkIn;
//                LocalTime checkOut;
//
//                if (isAbsent) {
//                    checkIn = LocalTime.of(9, 0);
//                    checkOut = LocalTime.of(9, 0);
//                } else {
//                    checkIn = randomTimeBetween(LocalTime.of(8, 10, 20), LocalTime.of(9, 10, 53));
//
//                    // 토요일은 특별 퇴근 시간
//                    if (isSaturday) {
//                        checkOut = randomTimeBetween(LocalTime.of(15, 0, 10), LocalTime.of(16, 10, 10));
//                    } else {
//                        checkOut = randomTimeBetween(LocalTime.of(18, 0, 10), LocalTime.of(19, 15, 15));
//                    }
//                }
//
//                MapSqlParameterSource attParams = new MapSqlParameterSource()
//                        .addValue("employeeId", employeeNumber)
//                        .addValue("attendanceDate", date)
//                        .addValue("checkInTime", checkIn)
//                        .addValue("checkOutTime", checkOut)
//                        .addValue("status", determineAttendanceStatus(status, checkIn, isSaturday));
//
//                batchParamsAttendance.add(attParams);
//
//                if (batchParamsAttendance.size() >= BATCH_SIZE) {
//                    System.out.println("done attendance");
//                    batchInsertAttendances(batchParamsAttendance);
//                    batchParamsAttendance.clear();
//                }
//            }

        }
                // salary 남은 데이터 처리
//        if (!batchParamsSalary.isEmpty()) {
//            batchInsertSalaries(batchParamsSalary);
//        }

        // attendance 남은 데이터 처리
//        if (!batchParamsAttendance.isEmpty()) {
//            batchInsertAttendances(batchParamsAttendance);
//        }
    }


    @Transactional
    // 부서 저장
    private List<Department> saveDepartments() {
        List<Department> departments = List.of(
                new Department("HR"),
                new Department("Finance"),
                new Department("Engineering"),
                new Department("Sales"),
                new Department("Marketing"),
                new Department("Customer Support"),
                new Department("Legal"),
                new Department("Operations"),
                new Department("Research & Development"),
                new Department("IT")
        );
        return departmentRepository.saveAll(departments);
    }

    // JDBC batch insert 실행
    private void batchInsertEmployees(List<MapSqlParameterSource> batchParams) {
        String sql = """
            INSERT INTO employee (name, email, hashed_pwd, age, department_id) 
            VALUES (:name, :email, :password, :age, :department_id)
        """;
        jdbcTemplate.batchUpdate(sql, batchParams.toArray(new MapSqlParameterSource[0]));
    }

    // JDBC batch insert 실행
    private void batchInsertSalaries(List<MapSqlParameterSource> batchParams) {
        String sql = """
            INSERT INTO salary (employee_id, payment_date, bonus, amount, status) 
            VALUES (:employeeId, :paymentDate, :bonus, :amount, :status)
        """;
        jdbcTemplate.batchUpdate(sql, batchParams.toArray(new MapSqlParameterSource[0]));
    }
    private void batchInsertAttendances(List<MapSqlParameterSource> batchParams) {
        String sql = """
            INSERT INTO attendance (employee_id, check_in_time, check_out_time, attendance_date, status) 
            VALUES (:employeeId, :checkInTime, :checkOutTime, :attendanceDate, :status)
        """;
        jdbcTemplate.batchUpdate(sql, batchParams.toArray(new MapSqlParameterSource[0]));
    }


    // 랜덤 이름 생성
    private String generateRandomName() {
        String[] firstNames = {"John", "Jane", "James", "Jill", "Jack", "Jake", "Jennifer", "Jessica", "Jonathan", "Jordan"};
        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Martinez", "Hernandez", "Lopez", "Gonzalez"};
        Random random = new Random();
        return firstNames[random.nextInt(firstNames.length)] + " " + lastNames[random.nextInt(lastNames.length)];
    }

    // 유니크 이메일 생성
    private String generateUniqueEmail(Set<String> usedEmails) {
        Random random = new Random();
        String email;
        do {
            email = "user" + random.nextInt(1000000) + "@company.com";
        } while (usedEmails.contains(email));
        usedEmails.add(email);
        return email;
    }

    private LocalTime randomTimeBetween(LocalTime start, LocalTime end) {
        int startSec = start.toSecondOfDay();
        int endSec = end.toSecondOfDay();
        int randomSec = startSec + new Random().nextInt(endSec - startSec + 1);
        return LocalTime.ofSecondOfDay(randomSec);
    }

    private String determineAttendanceStatus(String baseStatus, LocalTime checkIn, boolean isSaturday) {
        if ("ABSENT".equals(baseStatus)) return "ABSENT";
        if (isSaturday) return "PRESENT"; // 토요일은 지각 없음
        return checkIn.isAfter(LocalTime.of(9, 0)) ? "LATE" : "PRESENT";
    }
}