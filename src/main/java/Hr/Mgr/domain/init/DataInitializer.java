package Hr.Mgr.domain.init;

import Hr.Mgr.domain.entity.Employee;
import Hr.Mgr.domain.entity.Department;
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

import java.util.*;

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

    private static final int BATCH_SIZE = 1000; // JDBC batch 크기 설정
    private static final int CREATE_EMPLOYEE_NUMBER = 10000; // 생성할 직원 수

    @Override
    @Transactional 
    public void run(String... args) throws Exception {

        if (!"create".equalsIgnoreCase(dataInitMode)) {
            System.out.println("✅ 데이터 초기화 스킵 (app.data-init = " + dataInitMode + ")");
            return;
        }

        Random random = new Random();

        // 부서 생성 및 저장
        List<Department> departments = saveDepartments();

        // 이메일 중복 방지를 위한 Set
        Set<String> usedEmails = new HashSet<>();

        // 직원 데이터 생성
        List<MapSqlParameterSource> batchParams = new ArrayList<>();

        for (int i = 0; i < CREATE_EMPLOYEE_NUMBER; i++) {
            String name = generateRandomName();
            String email = generateUniqueEmail(usedEmails);
            String password = bCryptPasswordEncoder.encode("password" + i);
            Integer age = 20 + random.nextInt(40);

            // 랜덤 부서 ID 할당 (NULL 가능)
            Long departmentId = random.nextDouble() < 0.1 ? null : departments.get(random.nextInt(departments.size())).getId();

            // SQL 파라미터 바인딩
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("name", name)
                    .addValue("email", email)
                    .addValue("password", password)
                    .addValue("age", age)
                    .addValue("department_id", departmentId);

            batchParams.add(params);

            // BATCH_SIZE 단위로 INSERT 실행
            if (batchParams.size() >= BATCH_SIZE) {
                System.out.println("done ");
                batchInsertEmployees(batchParams);
                batchParams.clear();
            }
        }

        // 남은 데이터 처리
        if (!batchParams.isEmpty()) {
            batchInsertEmployees(batchParams);
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
            INSERT INTO employee (name, email, hashed_pwd, age, department_id) 
            VALUES (:name, :email, :password, :age, :department_id)
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
}