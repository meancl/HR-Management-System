package Hr.Mgr.domain.dto;

import Hr.Mgr.domain.entity.Salary;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class SalaryResDto {
    private Long id;
    private Long employeeId;
    private BigDecimal amount;
    private BigDecimal bonus;
    private LocalDate paymentDate;
    private String status;

    public SalaryResDto(Salary salary) {
        this.id = salary.getId();
        this.employeeId = salary.getEmployee().getId();
        this.amount = salary.getAmount();
        this.bonus = salary.getBonus();
        this.paymentDate = LocalDate.of(salary.getPaymentDate().getYear(),
                salary.getPaymentDate().getMonthValue(),
                salary.getPaymentDate().getDayOfMonth() ) ;
        this.status = salary.getStatus().name();
    }
}
