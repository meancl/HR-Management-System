package Hr.Mgr.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id") // db 속성 name
    private Long id;

    @Column(nullable = false)
    private String hashedPwd;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer age;

    public Employee() {
    }

    public Employee(String name, String email ,String hashedPwd, Integer age) {
        this.hashedPwd = hashedPwd;
        this.email = email;
        this.name = name;
        this.age = age;
    }
}
