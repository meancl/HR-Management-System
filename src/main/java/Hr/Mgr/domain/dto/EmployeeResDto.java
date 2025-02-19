package Hr.Mgr.domain.dto;

import Hr.Mgr.domain.entity.Employee;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeResDto {
    private Long id;
    private String name;
    private String email;
    private Integer age;



    public EmployeeResDto(Employee employee) {
        this.id = employee.getId();
        this.name = employee.getName();
        this.email = employee.getEmail();
        this.age = employee.getAge();
    }
}
