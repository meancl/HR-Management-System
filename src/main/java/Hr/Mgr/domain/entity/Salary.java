package Hr.Mgr.domain.entity;

import Hr.Mgr.domain.enums.SalaryStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Salary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "salary_id")
    private Long id;

    // 직원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    // 기본급
    private BigDecimal amount;
    // 보너스
    private BigDecimal bonus = BigDecimal.ZERO;

    // 지급날짜
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    private SalaryStatus status = SalaryStatus.PENDING;

    public Salary(){}

    public Salary(Employee employee, BigDecimal amount, BigDecimal bonus, LocalDate paymentDate) {
        this.employee = employee;
        this.amount = amount;
        this.bonus = bonus;
        this.paymentDate = paymentDate;
    }
}
