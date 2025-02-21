package Hr.Mgr.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Setter
public class ReviewScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_score_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PerformanceReview performanceReview;

    @Column(nullable = false)
    private String criteria; // 평가 항목 (예: "업무 성과", "협업 능력")

    @Column(nullable = false)
    private Integer score; // 점수 (예: 1~5점)

    public ReviewScore(){}


}