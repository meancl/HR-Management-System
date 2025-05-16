package Hr.Mgr.domain.entity;

import Hr.Mgr.domain.enums.EmployeeStatus;
import Hr.Mgr.domain.enums.Role;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter
public class Employee implements UserDetails {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeStatus employeeStatus;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    public Employee() {
    }

    public Employee(String name, String email ,String hashedPwd, Integer age, Department department, Role role) {
        this.hashedPwd = hashedPwd;
        this.email = email;
        this.name = name;
        this.age = age;
        this.department = department;
        this.employeeStatus = EmployeeStatus.PROBATION;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return hashedPwd;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
