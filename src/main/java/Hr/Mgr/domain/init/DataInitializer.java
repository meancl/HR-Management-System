package Hr.Mgr.domain.init;

import Hr.Mgr.domain.entity.Employee;
import Hr.Mgr.domain.entity.Department;
import Hr.Mgr.domain.enums.VacationType;
import Hr.Mgr.domain.repository.DepartmentRepository;
import Hr.Mgr.domain.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.IntStream;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CommandLineRunner.class);
    @Value("${app.giant_data_init}") // YAML 변수 가져오기
    private String dataInitMode;
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private EmployeeService employeeService;

    private static final int BATCH_SIZE = 2000; // JDBC batch 크기 설정
    private static final int CREATE_EMPLOYEE_NUMBER = 12500; // 생성할 직원 수
    private Map<String, DepartmentPolicy> policies = new HashMap<>();
    private Random random = new Random();
    @Override
    @Transactional 
    public void run(String... args) throws Exception {

        if (!"create".equalsIgnoreCase(dataInitMode)) {
            System.out.println("✅ 데이터 초기화 스킵 (app.data-init = " + dataInitMode + ")");
            return;
        }

//        List<MapSqlParameterSource> batchParams = new ArrayList<>();
        /*
        * 직원 데이터 생성 코드
        * */
//        // 부서 생성 및 저장
        List<MapSqlParameterSource> batchParams = new ArrayList<>();
        List<Department> departments = saveDepartments();
        // 이메일 중복 방지를 위한 Set
        Set<String> usedEmails = new HashSet<>();

        // salary, vacation, attenedance
        writeDepartmentPolicies();
        LocalDate today = LocalDate.now();

        List<MapSqlParameterSource> batchParamsSalary = new ArrayList<>();
        List<MapSqlParameterSource> batchParamsAttendance = new ArrayList<>();
        List<MapSqlParameterSource> batchParamsVacation = new ArrayList<>();
        int priceUnit = 100000;
        int employeeInitNum = 100000;
        List<Integer> employeeList = new ArrayList<>();
        HashMap<Integer, String> employeeHashMap = new HashMap<>();
        HashMap<Integer, Integer> employeeAgeHashMap = new HashMap<>();

        // 직원 데이터 생성
        for (int id = employeeInitNum; id < employeeInitNum + CREATE_EMPLOYEE_NUMBER; id++) {

            logger.info("id 직원생성 : {}",id);
            String name = generateRandomName();
            String email = generateUniqueEmail(usedEmails);
            String password = bCryptPasswordEncoder.encode("password" + id);
            Integer age = 20 + random.nextInt(40);

            int randomeDepartment = random.nextInt(departments.size());

            // 랜덤 부서 ID 할당 (NULL 가능)
            Long departmentId = random.nextDouble() < 0.1 ? null : departments.get(randomeDepartment).getId();
            String departmentName = departments.get(randomeDepartment).getName();
            // SQL 파라미터 바인딩
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("id", id)
                    .addValue("name", name)
                    .addValue("email", email)
                    .addValue("password", password)
                    .addValue("age", age)
                    .addValue("departmentId", departmentId);

            batchParams.add(params);

            // BATCH_SIZE 단위로 INSERT 실행
            if (batchParams.size() >= BATCH_SIZE) {
                System.out.println("done ");
                batchInsertEmployees(batchParams);
                batchParams.clear();
            }

            // salary, attendance, vacation 작업 준비
            employeeHashMap.put(id, departmentName);
            employeeAgeHashMap.put(id, age);

            employeeList.add(id);
        }
        // 남아있는 batch 처리
        if (!batchParams.isEmpty())
            batchInsertEmployees(batchParams);

        for (Integer id : employeeList)
        {
            try {
                String departmentName = employeeHashMap.get(id);
                Integer age = employeeAgeHashMap.get(id);

                Integer employeeNumber = id;
                DepartmentPolicy policy = policies.get(departmentName);

                logger.info("{}번째 데이터 추가", employeeNumber);

                int maxMonthsFromAge = Math.min((age - 20) * 12, 25 * 12 + 4);
                int totalMonths = random.nextInt(maxMonthsFromAge - 9 + 1) + 9;
                // 시작 월 = 현재 날짜 - 총 근무 개월 수
                LocalDate startDate = today.minusMonths(totalMonths);

                BigDecimal baseSalary = BigDecimal.valueOf(policy.minBaseSalary + (random.nextInt(policy.maxBaseSalary - policy.minBaseSalary + 1) / priceUnit * priceUnit));

                // salary 작업
                for (int i = 0; i <= totalMonths; i++) {
                    LocalDate workMonth = startDate.plusMonths(i);
                    LocalDate paymentDate = workMonth.withDayOfMonth(15).plusMonths(1); // 다음달 15일 지급

                    // 매년 1월
                    if (workMonth.getMonthValue() == 1 && Math.random() <= policy.raiseProbability) {
                        int increasePercent = policy.salaryRaiseMin + random.nextInt(policy.salaryRaiseMax - policy.salaryRaiseMin + 1); // 0~15 + 5 → 5~20

                        BigDecimal multiplier = BigDecimal.valueOf(1 + (increasePercent / 100.0));

                        baseSalary = baseSalary
                                .multiply(multiplier)
                                .divide(new BigDecimal(priceUnit), 0, RoundingMode.HALF_UP)
                                .multiply(new BigDecimal(priceUnit));
                    }

                    // 보너스:
                    BigDecimal bonus = BigDecimal.ZERO;
                    if (random.nextInt(100) < (1 - policy.raiseProbability) * 100) {

                        bonus = new BigDecimal(500000)
                                .add(baseSalary.multiply(new BigDecimal("0.05"))
                                        .divide(new BigDecimal(priceUnit), 0, RoundingMode.HALF_UP) // 10으로 나눠서 반올림
                                        .multiply(new BigDecimal(priceUnit)));
                    }

                    // status 계산
                    String status = today.isBefore(paymentDate) ? "PENDING" : "PAID";


                    MapSqlParameterSource paramsSalary = new MapSqlParameterSource()
                            .addValue("employeeId", employeeNumber)
                            .addValue("amount", baseSalary)
                            .addValue("bonus", bonus)
                            .addValue("paymentDate", paymentDate)
                            .addValue("status", status);

                    batchParamsSalary.add(paramsSalary);

                    // BATCH_SIZE 단위로 INSERT 실행
                    if (batchParamsSalary.size() >= BATCH_SIZE) {
                        logger.info("salary batch process");
                        batchInsertSalaries(batchParamsSalary);
                        batchParamsSalary.clear();
                    }
                }


                // vacation 작업
                Set<LocalDate> vacationDates = new HashSet<>();
                Map<LocalDate, VacationType> vacationMap = new HashMap<>();
                int years = totalMonths / 12;
                for (int y = 0; y < years; y++) {
                    LocalDate yearStart = startDate.plusYears(y);
                    LocalDate yearEnd = yearStart.plusYears(1).minusDays(1);

                    int annualTarget = 10 + random.nextInt(3); // 연차 10~12일
                    int sickTarget = random.nextInt(3);    // 병가 0~3일

                    // 연차 생성 (주말 제외, 시즌 편중)
                    generateVacationDays(batchParamsVacation, vacationDates, vacationMap, employeeNumber, annualTarget, VacationType.ANNUAL, yearStart, yearEnd, true);
                    // 병가 생성 (주말 제외, 무작위)
                    generateVacationDays(batchParamsVacation, vacationDates, vacationMap, employeeNumber, sickTarget, VacationType.SICK, yearStart, yearEnd, false);
                }

                // attendance 작업
                LocalDate attendanceStart = startDate.withDayOfMonth(1);
                LocalDate attendanceEnd = today;
                // 날짜 순회 (월~토 조건 포함)
                for (LocalDate date = attendanceStart; !date.isAfter(attendanceEnd); date = date.plusDays(1)) {

                    DayOfWeek dayOfWeek = date.getDayOfWeek();

                    boolean isWeekday = dayOfWeek != DayOfWeek.SUNDAY;
                    boolean isSaturday = dayOfWeek == DayOfWeek.SATURDAY;

                    boolean allowSaturday = isSaturday && random.nextInt(100) < 5;
                    if (!isWeekday || (isSaturday && !allowSaturday)) {
                        continue; // 일요일 제외, 토요일은 5% 확률만 허용
                    }

                    // 출근 상태 결정
                    String status;

                    LocalTime checkIn;
                    LocalTime checkOut;

                    VacationType vacationType = vacationMap.get(date);

                    if (vacationType != null) {
                        status = "HOLIDAY";
                        checkIn = checkOut = null;
                    } else {
                        checkIn = randomTimeBetween(policy.baseStartTime.minusMinutes(30), policy.baseStartTime.plusMinutes(20));
                        boolean isLate = checkIn.isAfter(policy.baseStartTime.plusMinutes(10));
                        status = isLate ? "LATE" : "PRESENT";

                        if (isSaturday) {
                            checkOut = randomTimeBetween(LocalTime.of(15, 0), LocalTime.of(16, 10));
                        } else {
                            if (random.nextDouble() < policy.overtimeProbability) {
                                int over = policy.overtimeMinMinutes + random.nextInt(policy.overtimeMaxMinutes - policy.overtimeMinMinutes + 1);
                                checkOut = policy.baseEndTime.plusMinutes(over);
                            } else {
                                checkOut = randomTimeBetween(policy.baseEndTime, policy.baseEndTime.plusMinutes(70));
                            }
                        }
                    }

                    MapSqlParameterSource attParams = new MapSqlParameterSource()
                            .addValue("employeeId", employeeNumber)
                            .addValue("attendanceDate", date)
                            .addValue("checkInTime", checkIn)
                            .addValue("checkOutTime", checkOut)
                            .addValue("status", status);

                    batchParamsAttendance.add(attParams);

                    if (batchParamsAttendance.size() >= BATCH_SIZE) {
                        logger.info("attendance batch process");
                        batchInsertAttendances(batchParamsAttendance);
                        batchParamsAttendance.clear();
                    }
                }
            }
            catch (Exception e){
                logger.warn("익셉션 발생 : {}", e);
            }
        }

        try {
            if (!batchParamsSalary.isEmpty())
                batchInsertSalaries(batchParamsSalary);
        }
        catch (Exception e){}
        try {
            if (!batchParamsAttendance.isEmpty())
                batchInsertVacations(batchParamsVacation);
        }
        catch (Exception e){

        }
        try {
            if (!batchParamsAttendance.isEmpty())
                batchInsertAttendances(batchParamsAttendance);
        }
        catch (Exception e){

        }





    }

    private void writeDepartmentPolicies() {
        policies.put("HR", new DepartmentPolicy(2500000, 3500000, 3, 7, 0.3,
                LocalTime.of(9, 0), LocalTime.of(18, 0), 0.1, 30, 60));

        policies.put("Finance", new DepartmentPolicy(3000000, 4500000, 5, 10, 0.4,
                LocalTime.of(8, 30), LocalTime.of(18, 30), 0.2, 60, 120));

        policies.put("Engineering", new DepartmentPolicy(4000000, 7000000, 8, 15, 0.6,
                LocalTime.of(10, 0), LocalTime.of(19, 0), 0.5, 60, 180));

        policies.put("Sales", new DepartmentPolicy(2800000, 4200000, 5, 12, 0.5,
                LocalTime.of(9, 0), LocalTime.of(18, 30), 0.3, 30, 90));

        policies.put("Marketing", new DepartmentPolicy(2700000, 4000000, 4, 9, 0.35,
                LocalTime.of(9, 30), LocalTime.of(18, 30), 0.2, 30, 60));

        policies.put("Customer Support", new DepartmentPolicy(2200000, 3000000, 3, 6, 0.25,
                LocalTime.of(8, 0), LocalTime.of(17, 0), 0.15, 30, 45));

        policies.put("Legal", new DepartmentPolicy(3500000, 5500000, 6, 10, 0.4,
                LocalTime.of(9, 0), LocalTime.of(18, 0), 0.2, 60, 90));

        policies.put("Operations", new DepartmentPolicy(2600000, 3800000, 4, 8, 0.3,
                LocalTime.of(8, 30), LocalTime.of(17, 30), 0.25, 45, 75));

        policies.put("Research & Development", new DepartmentPolicy(3700000, 6000000, 7, 12, 0.5,
                LocalTime.of(10, 0), LocalTime.of(19, 30), 0.4, 90, 150));

        policies.put("IT", new DepartmentPolicy(3200000, 5000000, 6, 11, 0.45,
                LocalTime.of(9, 0), LocalTime.of(18, 30), 0.35, 60, 120));
    }

    private void generateVacationDays(
            List<MapSqlParameterSource> targetList,
            Set<LocalDate> vacationSet,
            Map<LocalDate, VacationType> vacationMap,
            Integer employeeId,
            int totalDays,
            VacationType type,
            LocalDate from,
            LocalDate to,
            boolean seasonal
    ) {
        int used = 0;
        int attempts = 0;
        int maxAttempts = totalDays * 10;

        while (used < totalDays && attempts < maxAttempts) {
            // 지정된 범위 내 랜덤 날짜 선택
            LocalDate tryDate = from.plusDays(random.nextInt((int) ChronoUnit.DAYS.between(from, to)));

            // 주말은 제외
            DayOfWeek dow = tryDate.getDayOfWeek();
            if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
                attempts++;
                continue;
            }

            // seasonal == true 일 때만 휴가 시즌 분산 적용
            boolean assignHere;
            int month = tryDate.getMonthValue();

            if (seasonal) {
                if (month == 7 || month == 8 || month == 12 || month == 1) {
                    assignHere = random.nextDouble() < 0.65; // 65% 확률로 통과
                } else {
                    assignHere = true;
                }
            } else {
                assignHere = true;
            }

            // 이미 배정된 날짜는 스킵
            if (!assignHere || vacationSet.contains(tryDate)) {
                attempts++;
                continue;
            }

            vacationSet.add(tryDate);
            vacationMap.put(tryDate, type);

            targetList.add(new MapSqlParameterSource()
                    .addValue("employeeId", employeeId)
                    .addValue("type", type.name())
                    .addValue("startDate", tryDate)
                    .addValue("endDate", tryDate)
                    .addValue("approved", true)
                    .addValue("paid", type == VacationType.ANNUAL)
                    .addValue("totalDays", 1));

            used++;
            attempts++;

            if (targetList.size() >= BATCH_SIZE) {
                logger.info("vacation batch process");
                batchInsertVacations(targetList);
                targetList.clear();
            }

        }
    }



    public static class DepartmentPolicy {
        public int minBaseSalary;
        public int maxBaseSalary;
        public int salaryRaiseMin;
        public int salaryRaiseMax;
        public double raiseProbability;

        public LocalTime baseStartTime;
        public LocalTime baseEndTime;

        public double overtimeProbability;
        public int overtimeMinMinutes;
        public int overtimeMaxMinutes;

        public DepartmentPolicy(int minBaseSalary, int maxBaseSalary, int salaryRaiseMin, int salaryRaiseMax,
                                double raiseProbability, LocalTime baseStartTime, LocalTime baseEndTime,
                                double overtimeProbability, int overtimeMinMinutes, int overtimeMaxMinutes) {
            this.minBaseSalary = minBaseSalary;
            this.maxBaseSalary = maxBaseSalary;
            this.salaryRaiseMin = salaryRaiseMin;
            this.salaryRaiseMax = salaryRaiseMax;
            this.raiseProbability = raiseProbability;
            this.baseStartTime = baseStartTime;
            this.baseEndTime = baseEndTime;
            this.overtimeProbability = overtimeProbability;
            this.overtimeMinMinutes = overtimeMinMinutes;
            this.overtimeMaxMinutes = overtimeMaxMinutes;
        }
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
            INSERT INTO employee (employee_id, name, email, hashed_pwd, age, department_id) 
            VALUES (:id, :name, :email, :password, :age, :departmentId)
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
            INSERT INTO attendance_partitioned (employee_id, check_in_time, check_out_time, attendance_date, status) 
            VALUES (:employeeId, :checkInTime, :checkOutTime, :attendanceDate, :status)
        """;
        jdbcTemplate.batchUpdate(sql, batchParams.toArray(new MapSqlParameterSource[0]));
    }
    private void batchInsertVacations(List<MapSqlParameterSource> batchParams) {
        String sql = """
            INSERT INTO vacation (employee_id, total_days, end_date, start_date, paid, approved, type) 
            VALUES (:employeeId, :totalDays, :endDate, :startDate, :paid, :approved, :type)
        """;
        jdbcTemplate.batchUpdate(sql, batchParams.toArray(new MapSqlParameterSource[0]));
    }

    // 랜덤 이름 생성
    private String generateRandomName() {

        String[] firstNames = {
                "John", "Jane", "James", "Jill", "Jack", "Jake", "Jennifer", "Jessica", "Jonathan", "Jordan",
                "Emily", "Daniel", "Emma", "Michael", "Olivia", "David", "Sophia", "Chris", "Isabella", "Brian",
                "Amy", "Ethan", "Ashley", "Andrew", "Hannah", "Noah", "Abigail", "Matthew", "Mia", "Luke"
        };

        String[] lastNames = {
                "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Martinez", "Hernandez", "Lopez", "Gonzalez",
                "Clark", "Lewis", "Walker", "Hall", "Allen", "Young", "King", "Wright", "Scott", "Torres",
                "Nguyen", "Hill", "Green", "Adams", "Baker", "Nelson", "Carter", "Mitchell", "Perez", "Roberts"
        };
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

}