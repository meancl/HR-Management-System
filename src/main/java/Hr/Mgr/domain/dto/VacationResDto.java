package Hr.Mgr.domain.dto;

import Hr.Mgr.domain.entity.Vacation;
import Hr.Mgr.domain.enums.VacationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class VacationResDto {
    private Long id;
    private Long employeeId;
    private VacationType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean approved;
    private Boolean paid;

    public VacationResDto(Vacation vacation) {
        this.id = vacation.getId();
        this.employeeId = vacation.getEmployee().getId();
        this.type = vacation.getType();
        this.startDate = vacation.getStartDate();
        this.endDate = vacation.getEndDate();
        this.approved = vacation.isApproved();
        this.paid = vacation.isPaid();
    }
}
