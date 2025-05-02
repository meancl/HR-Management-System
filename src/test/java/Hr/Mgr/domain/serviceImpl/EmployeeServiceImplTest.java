package Hr.Mgr.domain.serviceImpl;

import Hr.Mgr.domain.dto.EmployeeReqDto;
import Hr.Mgr.domain.dto.EmployeeResDto;
import Hr.Mgr.domain.entity.Employee;
import Hr.Mgr.domain.enums.EmployeeStatus;
import Hr.Mgr.domain.exception.EmployeeNotFoundException;
import Hr.Mgr.domain.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeServiceImplTest {

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("직원 생성 성공")
    void testCreateEmployee() {
        EmployeeReqDto dto = new EmployeeReqDto();
        dto.setAge(30);
        dto.setEmail("hong@test.com");
        dto.setPassword("pass123");
        dto.setName("홍길동");

        Employee saved = new Employee();
        saved.setId(1L);
        saved.setName(dto.getName());
        saved.setEmail(dto.getEmail());
        saved.setAge(dto.getAge());
        saved.setEmployeeStatus(EmployeeStatus.PROBATION);
        saved.setHashedPwd("encoded-pass");

        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encoded-pass");
        when(employeeRepository.save(any(Employee.class))).thenReturn(saved);

        EmployeeResDto result = employeeService.createEmployee(dto);

        assertThat(result.getName()).isEqualTo(dto.getName());
        assertThat(result.getEmail()).isEqualTo(dto.getEmail());
        assertThat(result.getAge()).isEqualTo(dto.getAge());
        assertThat(result.getStatus()).isEqualTo(EmployeeStatus.PROBATION);
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("직원 조회 실패 - 존재하지 않음")
    void testFindEmployeeEntityById_NotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.findEmployeeEntityById(999L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("직원을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("직원 전체 조회")
    void testFindAllEmployeeDtos() {
        Employee emp = new Employee();
        emp.setId(1L);
        emp.setName("김철수");
        emp.setEmail("kim@test.com");
        emp.setAge(28);
        emp.setEmployeeStatus(EmployeeStatus.ACTIVE);

        Page<Employee> page = new PageImpl<>(List.of(emp));
        when(employeeRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<EmployeeResDto> result = employeeService.findAllEmployeeDtos(PageRequest.of(0, 10));
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("김철수");
    }

    @Test
    @DisplayName("직원 정보 수정")
    void testUpdateEmployee() {
        Employee emp = new Employee();
        emp.setId(1L);
        emp.setName("김길동");
        emp.setEmail("old@test.com");
        emp.setAge(25);
        emp.setHashedPwd("hashed-old");

        EmployeeReqDto dto = new EmployeeReqDto();
        dto.setAge(27);
        dto.setEmail("new@test.com");
        dto.setPassword("hashed-new");
        dto.setName("홍길동");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        when(passwordEncoder.matches(dto.getPassword(), emp.getHashedPwd())).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("hashed-new");

        employeeService.updateEmployee(1L, dto);

        assertThat(emp.getEmail()).isEqualTo("new@test.com");
        assertThat(emp.getAge()).isEqualTo(27);
        assertThat(emp.getHashedPwd()).isEqualTo("hashed-new");
    }

    @Test
    @DisplayName("직원 삭제 - 상태 변경")
    void testDeleteEmployee() {
        Employee emp = new Employee();
        emp.setId(1L);
        emp.setEmployeeStatus(EmployeeStatus.ACTIVE);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));

        employeeService.deleteEmployee(1L);

        assertThat(emp.getEmployeeStatus()).isEqualTo(EmployeeStatus.TERMINATED);
    }
}
