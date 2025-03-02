package Hr.Mgr.domain.entity;

import Hr.Mgr.domain.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Setter
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate attendanceDate; // 출근 날짜

    private LocalTime checkInTime; // 출근 시간

    private LocalTime checkOutTime; // 퇴근 시간

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    // 출퇴근 시간을 체크하는 메서드 (사용 아직 안할 예정)
    public Duration getTotalWorkDuration() {
        if (checkInTime != null && checkOutTime != null) {
            return Duration.between(checkInTime, checkOutTime);
        }
        return Duration.ZERO;
    }

    protected Attendance(){}

    public Attendance(Employee employee, LocalDate attendanceDate, LocalTime checkInTime, LocalTime checkOutTime, AttendanceStatus attendanceStatus ){
        this.employee = employee;
        this.attendanceDate = attendanceDate;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.status = attendanceStatus;
    }
}
