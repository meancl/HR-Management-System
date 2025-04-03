package Hr.Mgr.domain.entity;

import Hr.Mgr.domain.enums.VacationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Vacation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee; // 휴가를 사용하는 직원

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VacationType type; // 휴가 종류 (연차, 병가 등)

    @Column(nullable = false)
    private LocalDate startDate; // 휴가 시작일

    @Column(nullable = false)
    private LocalDate endDate; // 휴가 종료일

    @Column(nullable = false)
    private boolean approved = false; // 승인 여부

    @Column(nullable = false)
    private boolean paid; // 유급 여부

    @Column(nullable = false)
    private int totalDays; // 총 휴가 일수

    @PrePersist
    @PreUpdate
    public void calculateTotalDays() {
        this.totalDays = (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
    }

    public Vacation(){}
    public Vacation(Employee employee, VacationType vacationType, LocalDate startDate, LocalDate endDate){
        this.employee = employee;
        this.type = vacationType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.paid = false;
        this.approved = false;
    }
}
