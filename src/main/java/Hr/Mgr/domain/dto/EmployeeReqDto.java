package Hr.Mgr.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeReqDto {
    private String name;
    private String email;
    private String password; // 원본 비밀번호 (해싱 필요)
    private Integer age;

}
