package Hr.Mgr.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeResDto {
    private Long id;
    private String name;
    private String email;
    private Integer age;

    public EmployeeResDto(Long id, String name, String email, int age) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
    }
}
