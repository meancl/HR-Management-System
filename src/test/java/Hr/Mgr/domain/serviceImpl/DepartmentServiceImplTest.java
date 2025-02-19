package Hr.Mgr.domain.serviceImpl;


import Hr.Mgr.domain.dto.DepartmentDto;
import Hr.Mgr.domain.dto.EmployeeResDto;
import Hr.Mgr.domain.entity.Department;
import Hr.Mgr.domain.entity.Employee;
import Hr.Mgr.domain.repository.DepartmentRepository;
import Hr.Mgr.domain.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private Department department;
    private Employee employee;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setId(1L);
        department.setName("HR");

        employee = new Employee();
        employee.setId(100L);
        employee.setName("John Doe");
        employee.setDepartment(department);
    }

    @Test
    void createDepartment_Success() {
        // Given
        DepartmentDto dto = new DepartmentDto(department);
        dto.setName("Finance");

        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> {
            Department savedDepartment = invocation.getArgument(0);
            savedDepartment.setId(2L);
            return savedDepartment;
        });

        // When
        DepartmentDto result = departmentService.createDepartment(dto);

        // Then
        assertNotNull(result);
        assertEquals("Finance", result.getName());
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void listDepartments_Success() {
        // Given
        when(departmentRepository.findAll()).thenReturn(List.of(department));

        // When
        List<DepartmentDto> result = departmentService.listDepartments();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("HR", result.get(0).getName());
    }

    @Test
    void listEmployeesByDepartment_Success() {
        // Given
        department.setEmployees(List.of(employee));
        when(departmentRepository.findById(anyLong())).thenReturn(Optional.of(department));

        // When
        List<EmployeeResDto> result = departmentService.listEmployeesByDepartment(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getName());
    }

    @Test
    void listEmployeesByDepartment_NotFound() {
        // Given
        when(departmentRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Then
        assertThrows(IllegalArgumentException.class, () -> departmentService.listEmployeesByDepartment(1L));
    }

    @Test
    void findDepartmentByEmployee_Success() {
        // Given
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));

        // When
        DepartmentDto result = departmentService.findDepartmentByEmployee(100L);

        // Then
        assertNotNull(result);
        assertEquals("HR", result.getName());
    }

    @Test
    void findDepartmentByEmployee_NotFound() {
        // Given
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Then
        assertThrows(IllegalArgumentException.class, () -> departmentService.findDepartmentByEmployee(100L));
    }

    @Test
    void findDepartmentByEmployee_NoDepartment() {
        // Given
        employee.setDepartment(null);
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(employee));

        // Then
        assertThrows(RuntimeException.class, () -> departmentService.findDepartmentByEmployee(100L));
    }

    @Test
    void updateDepartment_Success() {
        // Given
        DepartmentDto updatedDto = new DepartmentDto(department);
        updatedDto.setName("IT");

        when(departmentRepository.findById(anyLong())).thenReturn(Optional.of(department));
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        DepartmentDto result = departmentService.updateDepartment(1L, updatedDto);

        // Then
        assertNotNull(result);
        assertEquals("IT", result.getName());
        verify(departmentRepository).save(department);
    }

    @Test
    void updateDepartment_NotFound() {
        // Given
        when(departmentRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Then
        assertThrows(RuntimeException.class, () -> departmentService.updateDepartment(1L, new DepartmentDto(department)));
    }

    @Test
    void deleteDepartment_Success() {
        // Given
        when(departmentRepository.findById(anyLong())).thenReturn(Optional.of(department));
        doNothing().when(departmentRepository).delete(any(Department.class));

        // When
        departmentService.deleteDepartment(1L);

        // Then
        verify(departmentRepository).delete(department);
    }

    @Test
    void deleteDepartment_NotFound() {
        // Given
        when(departmentRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Then
        assertThrows(RuntimeException.class, () -> departmentService.deleteDepartment(1L));
    }
}
