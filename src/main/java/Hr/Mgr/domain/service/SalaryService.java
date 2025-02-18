package Hr.Mgr.domain.service;


import Hr.Mgr.domain.dto.SalaryReqDto;
import Hr.Mgr.domain.dto.SalaryResDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface SalaryService {

    SalaryResDto createSalary(SalaryReqDto dto);
    SalaryResDto getLatestSalaryByEmployee(Long employeeId);
    SalaryResDto getSalaryById(Long salaryId);
    List<SalaryResDto> getSalariesByEmployee(Long employeeId);
    List<SalaryResDto> getSalariesByDateRange(Long employeeId, LocalDate startDate, LocalDate endDate);
    SalaryResDto updateSalary(Long salaryId, SalaryReqDto dto);
    void deleteSalary(Long salaryId);
    SalaryResDto markAsPaid(Long salaryId);

    void updateSalaryAmountAndLocalDate(Long salaryId, BigDecimal newAmount, LocalDate newDate);
}
