package Hr.Mgr.domain.serviceImpl;

import Hr.Mgr.domain.dto.DepartmentDto;
import Hr.Mgr.domain.entity.Department;
import Hr.Mgr.domain.exception.DepartmentNotFoundException;
import Hr.Mgr.domain.repository.DepartmentRepository;
import Hr.Mgr.domain.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {


    private final DepartmentRepository departmentRepository;
    @Override
    public DepartmentDto createDepartment(DepartmentDto departmentDto) {
        Department department = new Department();
        department.setName(departmentDto.getName());
        return  new DepartmentDto(departmentRepository.save(department));
    }


    @Override
    @Transactional(readOnly = true)
    public DepartmentDto findDepartmentByEmployeeId(Long employeeId) {
        Department byEmployeeId = departmentRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new DepartmentNotFoundException("department not found exception with empId : " + employeeId ));
        return new DepartmentDto(byEmployeeId);
    }

    @Override
    public Department findDepartmentEntityById(Long departmentId) {
        Department byEmployeeId = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException("department not found exception with empId : " + departmentId ));
        return byEmployeeId;
    }


}
