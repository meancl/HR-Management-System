package Hr.Mgr.domain.service;

import Hr.Mgr.domain.dto.DepartmentDto;
import Hr.Mgr.domain.dto.EmployeeResDto;

import java.util.List;

public interface DepartmentService {
    DepartmentDto createDepartment(DepartmentDto departmentDto);
    List<DepartmentDto> listDepartments();
    List<EmployeeResDto> listEmployeesByDepartment(Long departmentId);
    DepartmentDto findDepartmentByEmployee(Long employeeId);
    DepartmentDto updateDepartment(Long departmentId, DepartmentDto departmentDto);
    void deleteDepartment(Long departmentId);

}