package Hr.Mgr.domain.entity;

import Hr.Mgr.domain.enums.ReviewStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
public class PerformanceReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee; // 평가 대상

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private Employee reviewer; // 평가자

    @Column(nullable = false)
    private LocalDate reviewDate; // 평가 날짜

    @Column(length = 500)
    private String comments; // 평가 의견

    @Enumerated(EnumType.STRING)
    private ReviewStatus status; // 평가 상태 (대기, 완료 등)

    public PerformanceReview(){}
}
