package Hr.Mgr.domain.service;

import Hr.Mgr.domain.dto.EmployeeReqDto;
import Hr.Mgr.domain.dto.EmployeeResDto;

import java.util.List;


public interface EmployeeService {
    void createEmployee(EmployeeReqDto request);
    void updateEmployee(Long id, EmployeeReqDto request);
    EmployeeResDto getEmployeeById(Long id);
    List<EmployeeResDto> getEmployees();
    void deleteEmployee(Long id);
}
