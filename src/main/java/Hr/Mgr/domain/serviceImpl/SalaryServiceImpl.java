package Hr.Mgr.domain.serviceImpl;

import Hr.Mgr.domain.dto.SalaryReqDto;
import Hr.Mgr.domain.dto.SalaryResDto;
import Hr.Mgr.domain.entity.Employee;
import Hr.Mgr.domain.entity.Salary;
import Hr.Mgr.domain.enums.SalaryStatus;
import Hr.Mgr.domain.repository.EmployeeRepository;
import Hr.Mgr.domain.repository.SalaryRepository;
import Hr.Mgr.domain.service.SalaryService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalaryServiceImpl implements SalaryService {
    private final SalaryRepository salaryRepository;
    private final EmployeeRepository employeeRepository;


    @Transactional
    public SalaryResDto createSalary(SalaryReqDto dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        Salary salary = new Salary(employee, dto.getAmount(), dto.getBonus(), dto.getPaymentDate());

        return new SalaryResDto(salaryRepository.save(salary));
    }

    @Transactional(readOnly = true)
    public SalaryResDto getLatestSalaryByEmployee(Long employeeId) {
        Optional<Salary> latestSalary = salaryRepository.findTopByEmployeeIdOrderByPaymentDateDesc(employeeId);
        return latestSalary.map(SalaryResDto::new).orElseThrow(() -> new IllegalArgumentException("No salary found for employee"));
    }

    @Override
    @Transactional(readOnly = true)
    public SalaryResDto getSalaryById(Long salaryId) {
        return salaryRepository.findById(salaryId).map(SalaryResDto::new)
                .orElseThrow(() -> new IllegalArgumentException("No salary found for employee"));
    }

    @Override
    public List<SalaryResDto> getSalariesByEmployee(Long employeeId) {
        return salaryRepository.findByEmployeeId(employeeId)
                .stream().map(SalaryResDto::new).toList();
    }

    @Transactional(readOnly = true)
    public List<SalaryResDto> getSalariesByDateRange(Long employeeId, LocalDate startDate, LocalDate endDate) {
        return salaryRepository.findByEmployeeIdAndPaymentDateBetween(employeeId, startDate, endDate)
                .stream().map(SalaryResDto::new).collect(Collectors.toList());
    }

    @Transactional
    public SalaryResDto updateSalary(Long salaryId, SalaryReqDto dto) {
        Salary salary = salaryRepository.findById(salaryId)
                .orElseThrow(() -> new IllegalArgumentException("Salary not found"));
        if (dto.getAmount() != null) salary.setAmount(dto.getAmount());
        if (dto.getBonus() != null) salary.setBonus(dto.getBonus());
        if (dto.getPaymentDate() != null) salary.setPaymentDate(dto.getPaymentDate());
        return new SalaryResDto(salaryRepository.save(salary));
    }

    @Transactional
    public void deleteSalary(Long salaryId) {
        Salary salary = salaryRepository.findById(salaryId)
                .orElseThrow(() -> new IllegalArgumentException("Salary not found"));
        salaryRepository.delete(salary);
    }

    @Transactional
    public SalaryResDto markAsPaid(Long salaryId) {
        Salary salary = salaryRepository.findById(salaryId)
                .orElseThrow(() -> new IllegalArgumentException("Salary not found"));
        salary.setStatus(SalaryStatus.PAID);
        return new SalaryResDto(salaryRepository.save(salary));
    }

    @Transactional
    public void updateSalaryAmountAndLocalDate(Long salaryId, BigDecimal newAmount, LocalDate newDate) {
        salaryRepository.updateSalaryAmountAndLocalDate(salaryId, newAmount, newDate);
    }
}
