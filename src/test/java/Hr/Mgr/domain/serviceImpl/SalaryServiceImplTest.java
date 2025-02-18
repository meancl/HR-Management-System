package Hr.Mgr.domain.serviceImpl;

import Hr.Mgr.domain.dto.EmployeeReqDto;
import Hr.Mgr.domain.dto.SalaryReqDto;
import Hr.Mgr.domain.dto.SalaryResDto;
import Hr.Mgr.domain.repository.SalaryRepository;
import Hr.Mgr.domain.service.EmployeeService;
import Hr.Mgr.domain.service.SalaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class SalaryServiceImplTest {

    @Autowired
    private SalaryService salaryService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private Long employeeId;

    private SalaryReqDto salaryReqDto;
    private SalaryResDto salary;

    @BeforeEach
    void createEmployee(){
        EmployeeReqDto employeeReqDto = new EmployeeReqDto();
        employeeReqDto.setName("민재");
        employeeReqDto.setEmail("sbe03253@naver.com");
        employeeReqDto.setPassword(bCryptPasswordEncoder.encode("passwd1234"));
        employeeReqDto.setAge(30);

        employeeId =  employeeService.createEmployee(employeeReqDto);

        salaryReqDto = new SalaryReqDto();
        salaryReqDto.setEmployeeId(employeeId);
        salaryReqDto.setAmount(new BigDecimal(2600000));
        salaryReqDto.setBonus( new BigDecimal(200000));
        salaryReqDto.setPaymentDate(LocalDate.of(2025, 1, 25));

        salary = salaryService.createSalary(salaryReqDto);
    }

    @Test
    void createSalary() {
        assertThat(salary.getAmount()).isEqualTo(new BigDecimal(2600000));
    }

    @Test
    void getLatestSalaryByEmployee() {
        SalaryResDto findSalary = salaryService.getLatestSalaryByEmployee(salary.getEmployeeId());

        assertThat(salary).usingRecursiveComparison()
                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .isEqualTo(findSalary);
    }

    @Test
    void getSalaries(){
        SalaryReqDto salaryReqDto2 = new SalaryReqDto();
        salaryReqDto2.setEmployeeId(employeeId);
        salaryReqDto2.setAmount(new BigDecimal(2600000));
        salaryReqDto2.setBonus( new BigDecimal(200000));
        salaryReqDto2.setPaymentDate(LocalDate.of(2025, 2, 25));

        SalaryResDto salary2 = salaryService.createSalary(salaryReqDto2);

        List<SalaryResDto> salariesByEmployee = salaryService.getSalariesByEmployee(employeeId);
        assertThat(salariesByEmployee.size()).isEqualTo(2);

    }

    @Test
    void getSalariesByDateRange() {


        SalaryReqDto salaryReqDto2 = new SalaryReqDto();
        salaryReqDto2.setEmployeeId(employeeId);
        salaryReqDto2.setAmount(new BigDecimal(2600000));
        salaryReqDto2.setBonus( new BigDecimal(200000));
        salaryReqDto2.setPaymentDate(LocalDate.of(2025, 2, 25));

        SalaryResDto salary2 = salaryService.createSalary(salaryReqDto2);

        List<SalaryResDto> salariesByDateRange = salaryService.getSalariesByDateRange(employeeId, LocalDate.of(2025, 1, 25), LocalDate.of(2025, 2, 25));
        assertThat(salariesByDateRange.size()).isEqualTo(2);

    }

    @Test
    void updateSalary() {
        salaryReqDto.setPaymentDate(LocalDate.of(2025, 2, 25));
        salaryReqDto.setBonus(new BigDecimal(0));
        salaryReqDto.setAmount(new BigDecimal(3000000));


        SalaryResDto salaryResDto = salaryService.updateSalary(salary.getId(), salaryReqDto);

        assertThat(salaryResDto.getPaymentDate()).isNotEqualTo(salary.getPaymentDate());
        assertThat(salaryResDto.getBonus()).usingComparator(BigDecimal::compareTo).isNotEqualTo(salary.getBonus());
        assertThat(salaryResDto.getAmount()).usingComparator(BigDecimal::compareTo).isNotEqualTo(salary.getAmount());

    }

    @Test
    void deleteSalary() {

        salaryService.deleteSalary(salary.getId());

        assertThrows(IllegalArgumentException.class,
                () -> salaryService.getLatestSalaryByEmployee(salary.getEmployeeId()));
    }

    @Test
    void markAsPaid() {

        String status = salary.getStatus();

        SalaryResDto salaryResDto = salaryService.markAsPaid(salary.getId());


        assertThat(salaryResDto.getStatus()).isNotEqualTo(status);
    }

    @Test
    void updateSalaryQuery() {
        salaryService.updateSalaryAmountAndLocalDate(salary.getId(),new BigDecimal(3000000), LocalDate.of(2025, 2, 25));

        SalaryResDto latestSalaryByEmployee = salaryService.getLatestSalaryByEmployee(salary.getEmployeeId());


        assertThat(latestSalaryByEmployee.getPaymentDate()).isNotEqualTo(salary.getPaymentDate());
        assertThat(latestSalaryByEmployee.getAmount()).usingComparator(BigDecimal::compareTo).isNotEqualTo(salary.getAmount());

    }

}