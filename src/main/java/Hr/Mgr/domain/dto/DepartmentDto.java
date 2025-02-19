package Hr.Mgr.domain.dto;

import Hr.Mgr.domain.entity.Department;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepartmentDto {
    private Long id;
    private String name;

    public DepartmentDto(Department department) {
        this.id = department.getId();
        this.name = department.getName();
    }
}
