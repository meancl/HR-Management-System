package Hr.Mgr.domain.dto;

import Hr.Mgr.domain.enums.VacationType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class VacationReqDto{
    private Long employeeId;
    private VacationType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean approved;
    private Boolean paid;
}
