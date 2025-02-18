package Hr.Mgr.domain.dto;

import Hr.Mgr.domain.enums.AttendanceStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class AttendanceReqDto {
    private Long employeeId;
    private LocalDate attendanceDate;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private AttendanceStatus attendanceStatus;
}
