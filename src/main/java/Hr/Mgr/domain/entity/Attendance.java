package Hr.Mgr.domain.entity;

import Hr.Mgr.domain.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "attendance_partitioned")
@Getter
@Setter
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
//    @OnDelete(action = OnDeleteAction.CASCADE)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate attendanceDate; // 출근 날짜

    private LocalTime checkInTime; // 출근 시간

    private LocalTime checkOutTime; // 퇴근 시간

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 출퇴근 시간을 체크하는 메서드 (사용 아직 안할 예정)
    public Duration getTotalWorkDuration() {
        if (checkInTime != null && checkOutTime != null) {
            return Duration.between(checkInTime, checkOutTime);
        }
        return Duration.ZERO;
    }

    public Attendance(){}

    public Attendance(Employee employee, LocalDate attendanceDate, LocalTime checkInTime, LocalTime checkOutTime, AttendanceStatus attendanceStatus ){
        this.employee = employee;
        this.attendanceDate = attendanceDate;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.status = attendanceStatus;
    }
}
