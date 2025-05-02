package Hr.Mgr.domain.serviceImpl;

import Hr.Mgr.domain.dto.DepartmentDto;
import Hr.Mgr.domain.entity.Department;
import Hr.Mgr.domain.exception.DepartmentNotFoundException;
import Hr.Mgr.domain.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DepartmentServiceImplTest {

    private DepartmentRepository departmentRepository;
    private DepartmentServiceImpl departmentService;

    @BeforeEach
    void setUp() {
        departmentRepository = mock(DepartmentRepository.class);
        departmentService = new DepartmentServiceImpl(departmentRepository);
    }

    @Test
    void createDepartment_shouldSaveAndReturnDto() {
        // given
        Department saved = new Department();
        saved.setId(1L);
        saved.setName("HR");

        DepartmentDto dto = new DepartmentDto(saved);

        when(departmentRepository.save(any())).thenReturn(saved);

        // when
        DepartmentDto result = departmentService.createDepartment(dto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("HR");
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void findDepartmentByEmployeeId_shouldReturnDto_whenDepartmentExists() {
        // given
        Long employeeId = 123L;
        Department department = new Department();
        department.setId(1L);
        department.setName("Engineering");

        when(departmentRepository.findByEmployeeId(employeeId)).thenReturn(Optional.of(department));

        // when
        DepartmentDto result = departmentService.findDepartmentByEmployeeId(employeeId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Engineering");
    }

    @Test
    void findDepartmentByEmployeeId_shouldThrowException_whenNotFound() {
        // given
        Long employeeId = 999L;
        when(departmentRepository.findByEmployeeId(employeeId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> departmentService.findDepartmentByEmployeeId(employeeId))
                .isInstanceOf(DepartmentNotFoundException.class)
                .hasMessageContaining("department not found exception");
    }
}
