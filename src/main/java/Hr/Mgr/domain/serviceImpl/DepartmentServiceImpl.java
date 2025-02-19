package Hr.Mgr.domain.serviceImpl;

import Hr.Mgr.domain.dto.DepartmentDto;
import Hr.Mgr.domain.dto.EmployeeResDto;
import Hr.Mgr.domain.entity.Department;
import Hr.Mgr.domain.entity.Employee;
import Hr.Mgr.domain.repository.DepartmentRepository;
import Hr.Mgr.domain.repository.EmployeeRepository;
import Hr.Mgr.domain.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {


    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public DepartmentDto createDepartment(DepartmentDto departmentDto) {
        Department department = new Department();
        department.setName(departmentDto.getName());

        return new DepartmentDto( departmentRepository.save(department));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentDto> listDepartments() {
        return departmentRepository.findAll()
                .stream().map(DepartmentDto::new)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResDto> listEmployeesByDepartment(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("no department found"));

        return department.getEmployees()
                .stream().map(EmployeeResDto::new)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentDto findDepartmentByEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("No Employee found" + employeeId));

        if (employee.getDepartment() == null)
            throw new RuntimeException("직원이 부서에 속해있지 않습니다: " + employeeId);

        return new DepartmentDto(employee.getDepartment());
    }

    @Override
    @Transactional()
    public DepartmentDto updateDepartment(Long departmentId, DepartmentDto departmentDto) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("부서를 찾을 수 없음: " + departmentId));

        if(departmentDto.getName() != null) department.setName(departmentDto.getName());

        return new DepartmentDto(departmentRepository.save(department));
    }

    @Override
    @Transactional()
    public void deleteDepartment(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("부서를 찾을 수 없음: " + departmentId));
        departmentRepository.delete(department);
    }
}
