package Hr.Mgr.domain.service;

import Hr.Mgr.domain.dto.EmployeeReqDto;
import Hr.Mgr.domain.dto.EmployeeResDto;
import Hr.Mgr.domain.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface EmployeeService {
    Long createEmployee(EmployeeReqDto request);
    void updateEmployee(Long id, EmployeeReqDto request);
    EmployeeResDto findEmployeeDtoById(Long id);
    Employee findEmployeeEntityById(Long id);
    List<EmployeeResDto> findAllEmployeeDtos();
    Page<EmployeeResDto> findAllEmployeeDtos(Pageable pageable);
    void deleteEmployee(Long id);
    EmployeeResDto updateDepartment(Long employeeId, Long departmentId);
}
