package Hr.Mgr.domain.dto;

import Hr.Mgr.domain.entity.Attendance;
import Hr.Mgr.domain.enums.AttendanceStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class AttendanceResDto {
    private Long id;
    private Long employeeId;
    private LocalDate attendanceDate;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private AttendanceStatus status;


    public AttendanceResDto(Attendance attendance) {
        this.id = attendance.getId();
        this.employeeId = attendance.getEmployee().getId();
        this.attendanceDate = attendance.getAttendanceDate();
        this.checkInTime = attendance.getCheckInTime();
        this.checkOutTime = attendance.getCheckOutTime();
        this.status = attendance.getStatus();
    }
}
