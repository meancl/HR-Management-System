package Hr.Mgr.domain.service;

import Hr.Mgr.domain.dto.DepartmentDto;
import Hr.Mgr.domain.dto.EmployeeResDto;
import Hr.Mgr.domain.entity.Department;

import java.util.List;

public interface DepartmentService {
    DepartmentDto createDepartment(DepartmentDto departmentDto);
    Department createDepartmentEntity(DepartmentDto departmentDto);
    Department findDepartmentEntityById(Long departmentId);
    List<DepartmentDto> findAllDepartmentDtos();
    List<EmployeeResDto> findEmployeeDtosByDepartmentId(Long departmentId);
    DepartmentDto findDepartmentDtoByEmployeeId(Long employeeId);
    DepartmentDto updateDepartment(Long departmentId, DepartmentDto departmentDto);
    void deleteDepartment(Long departmentId);

}