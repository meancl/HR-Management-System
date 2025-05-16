package Hr.Mgr.domain.dto;

import Hr.Mgr.domain.enums.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeReqDto {
    private String name;
    private String email;
    private String password;
    private Integer age;
    private Long departmentId;
    private Role role;
}
