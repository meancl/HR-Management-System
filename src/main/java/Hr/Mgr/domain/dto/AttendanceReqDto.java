package Hr.Mgr.domain.dto;

import Hr.Mgr.domain.enums.AttendanceStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceReqDto {
    @JsonProperty("employeeId")
    private Long employeeId;

    @JsonProperty("attendanceDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate attendanceDate;

    @JsonProperty("checkInTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime checkInTime;

    @JsonProperty("checkOutTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime checkOutTime;

    @JsonProperty("attendanceStatus")
    private AttendanceStatus attendanceStatus;
    @Override
    public String toString(){
        return String.format("%s %s %s %s %s", employeeId, attendanceDate, checkInTime, checkOutTime, attendanceStatus);
    }
}
