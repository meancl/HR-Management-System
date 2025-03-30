package Hr.Mgr.domain.entity;

import Hr.Mgr.domain.converter.JsonMapConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@Entity
@Table( uniqueConstraints = {@UniqueConstraint(columnNames = {"employee_id", "year", "month"})})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuarterlyAttendanceStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer quarter;

    // 출근일 수 및 기본 통계
    @Column(nullable = false)
    private Integer presentDays;

    @Column(nullable = false)
    private Integer lateCount;

    // 초과 근무 시간 (분 단위)
    @Column(nullable = false)
    private Integer avgOvertimeMinutes;

    // 평균 근무 시간 (분 단위)
    @Column(nullable = false)
    private Integer avgWorkMinutes;

    // 평균 출근 시간
    @Column(nullable = false)
    private LocalTime avgStartTime;

    // 평균 퇴근 시간
    @Column(nullable = false)
    private LocalTime avgEndTime;

    // 주간 근무 시간 추이
    @Convert(converter = JsonMapConverter.class)
    private Map<String, Integer> weeklyWorkMinutes;

    // 휴일 출근 비율 (0.0000 ~ 1.0000)
    @Column(nullable = false)
    private Double holidayWorkRatio;


    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
