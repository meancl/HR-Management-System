package Hr.Mgr.domain.service;

import Hr.Mgr.domain.dto.EmployeeReqDto;
import Hr.Mgr.domain.dto.EmployeeResDto;
import Hr.Mgr.domain.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface EmployeeService {
    EmployeeResDto createEmployee(EmployeeReqDto request);
    void updateEmployee(Long id, EmployeeReqDto request);
    EmployeeResDto findEmployeeDtoById(Long id);
    Employee findEmployeeEntityById(Long id);
    Page<EmployeeResDto> findAllEmployeeDtos(Pageable pageable);
    void deleteEmployee(Long id);
}
