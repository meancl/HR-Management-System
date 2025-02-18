package Hr.Mgr.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class SalaryReqDto {
    private Long employeeId;
    private BigDecimal amount;
    private BigDecimal bonus;
    private LocalDate paymentDate;

    public SalaryReqDto(){}
}