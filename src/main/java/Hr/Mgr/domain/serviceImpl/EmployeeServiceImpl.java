package Hr.Mgr.domain.serviceImpl;

import Hr.Mgr.domain.dto.EmployeeReqDto;
import Hr.Mgr.domain.dto.EmployeeResDto;
import Hr.Mgr.domain.entity.Department;
import Hr.Mgr.domain.entity.Employee;
import Hr.Mgr.domain.enums.EmployeeStatus;
import Hr.Mgr.domain.exception.EmployeeNotFoundException;
import Hr.Mgr.domain.repository.DepartmentRepository;
import Hr.Mgr.domain.repository.EmployeeRepository;
import Hr.Mgr.domain.service.DepartmentService;
import Hr.Mgr.domain.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);
    private final EmployeeRepository employeeRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final DepartmentService departmentService;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, BCryptPasswordEncoder passwordEncoder, DepartmentService departmentService) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.departmentService = departmentService;
    }

    @Override
    public Long createEmployee(EmployeeReqDto request) {
        logger.info(" 회원가입 요청: 이메일={}, 이름={}", request.getEmail(), request.getName());

        try {
            Employee employee = new Employee();
            employee.setName(request.getName());
            employee.setEmail(request.getEmail());
            employee.setHashedPwd(passwordEncoder.encode(request.getPassword())); // ✅ 비밀번호 해싱
            employee.setAge(request.getAge());
            employee.setEmployeeStatus(EmployeeStatus.PROBATION);

            Employee save = employeeRepository.save(employee);
            logger.info(" 회원가입 성공: 이메일={}", request.getEmail());

            return save.getId();
        }
        catch (Exception e) {
            logger.error(" 회원가입 실패: 이메일={}, 에러={}", request.getEmail(), e.getMessage());
            throw e;
        }
    }

    @Override
    public void updateEmployee(Long id, EmployeeReqDto request) {
        Employee employee = findEmployeeEntityById(id);
        update(employee, request);
    }

    private void update(Employee employee, EmployeeReqDto request) {
        if(request.getAge() != null  && !employee.getAge().equals(request.getAge()) )
            employee.setAge(request.getAge());
        if(request.getEmail() != null && !request.getEmail().isEmpty() && !employee.getEmail().equals(request.getEmail()))
            employee.setEmail(request.getEmail());
        if(request.getPassword() != null && !request.getPassword().isEmpty() && !passwordEncoder.matches(request.getPassword(),employee.getHashedPwd()))
            employee.setHashedPwd(passwordEncoder.encode(request.getPassword()));
        if(request.getName() != null && !request.getName().isEmpty() && !employee.getName().equals(request.getName()))
            employee.setName(request.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResDto findEmployeeDtoById(Long employeeId) {
        return new EmployeeResDto(findEmployeeEntityById(employeeId));
    }

    @Override
    @Transactional(readOnly = true)
    public Employee findEmployeeEntityById(Long employeeId) {
        return employeeRepository.findWithDepartmentById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("해당 ID의 직원을 찾을 수 없습니다: " + employeeId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResDto> findAllEmployeeDtos(Pageable pageable) {

        return employeeRepository.findAll(pageable)
//                .filter(employee-> employee.getEmployeeStatus() != EmployeeStatus.TERMINATED)
                .map(EmployeeResDto::new);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResDto> findAllEmployeeDtos() {

        return employeeRepository.findAll().stream()
//                .filter(employee-> employee.getEmployeeStatus() != EmployeeStatus.TERMINATED)
                .map(EmployeeResDto::new).toList();
    }
    @Override
    @Transactional(readOnly = true)
    public List<Employee> findAllEmployeeEntities() {
        return employeeRepository.findAllEmployeesWithDepartment();
    }

    @Override
    public void deleteEmployee(Long id) {
        Employee employee = findEmployeeEntityById(id);
        employee.setEmployeeStatus(EmployeeStatus.TERMINATED);
//        employeeRepository.deleteById(id);
    }

    @Override
    public EmployeeResDto updateDepartment(Long employeeId, Long departmentId) {
        Department department = departmentService.findDepartmentEntityById(departmentId);
        Employee employee =  findEmployeeEntityById(employeeId);

        employee.setDepartment(department);
        department.getEmployees().add(employee);

        return new EmployeeResDto(employeeRepository.save(employee));

    }
}
