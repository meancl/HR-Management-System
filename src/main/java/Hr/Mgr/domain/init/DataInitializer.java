package Hr.Mgr.domain.init;

import Hr.Mgr.domain.entity.Employee;
import Hr.Mgr.domain.repository.EmployeeRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @PostConstruct
    public void init() {
        // 기본 데이터 삽입
        employeeRepository.save(new Employee("홍길동", "hong@company.com", bCryptPasswordEncoder.encode("password123"), 30));
        employeeRepository.save(new Employee("김철수", "kim@company.com", bCryptPasswordEncoder.encode("password456"), 28));
        employeeRepository.save(new Employee("이영희", "lee@company.com", bCryptPasswordEncoder.encode("password789"), 35));
        employeeRepository.save(new Employee("박지민", "park@company.com", bCryptPasswordEncoder.encode("password111"), 27));
        employeeRepository.save(new Employee("최강욱", "choi@company.com", bCryptPasswordEncoder.encode("password222"), 32));
        employeeRepository.save(new Employee("정유진", "jung@company.com", bCryptPasswordEncoder.encode("password333"), 29));
        employeeRepository.save(new Employee("나성민", "na@company.com", bCryptPasswordEncoder.encode("password444"), 33));
        employeeRepository.save(new Employee("오승현", "oh@company.com", bCryptPasswordEncoder.encode("password555"), 31));
        employeeRepository.save(new Employee("한지우", "han@company.com", bCryptPasswordEncoder.encode("password666"), 26));
        employeeRepository.save(new Employee("이준호", "lee.junho@company.com", bCryptPasswordEncoder.encode("password777"), 34));
        employeeRepository.save(new Employee("윤서영", "yoon@company.com", bCryptPasswordEncoder.encode("password888"), 27));
        employeeRepository.save(new Employee("김보람", "kim.boram@company.com", bCryptPasswordEncoder.encode("password999"), 29));
        employeeRepository.save(new Employee("임소연", "lim@company.com", bCryptPasswordEncoder.encode("password101"), 28));
        employeeRepository.save(new Employee("전은수", "jeon@company.com", bCryptPasswordEncoder.encode("password202"), 30));
        employeeRepository.save(new Employee("송하나", "song@company.com", bCryptPasswordEncoder.encode("password303"), 32));
    }
}