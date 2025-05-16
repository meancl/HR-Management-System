package Hr.Mgr.domain.service;

import Hr.Mgr.domain.dto.DepartmentDto;
import Hr.Mgr.domain.dto.EmployeeResDto;
import Hr.Mgr.domain.entity.Department;

import java.util.List;

public interface DepartmentService {
    DepartmentDto createDepartment(DepartmentDto departmentDto);
    DepartmentDto findDepartmentByEmployeeId(Long employeeId);
    Department findDepartmentEntityById(Long departmentId);

}