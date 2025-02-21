package Hr.Mgr.domain.dto;

import Hr.Mgr.domain.entity.PerformanceReview;
import Hr.Mgr.domain.entity.ReviewScore;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class PerformanceReviewDto {
    private Long id;
    private Long employeeId;
    private Long reviewerId;
    private LocalDate reviewDate;
    private List<ReviewScoreDto> scores;
    private String comments;

    public PerformanceReviewDto() {
    }

    public PerformanceReviewDto(PerformanceReview performanceReview,List<ReviewScore> reviewScore) {
        this.id = performanceReview.getId();
        this.employeeId = performanceReview.getEmployee().getId();
        this.reviewerId = performanceReview.getReviewer().getId();
        this.reviewDate = performanceReview.getReviewDate();
        this.comments = performanceReview.getComments();
        this.scores = reviewScore.stream().map(ReviewScoreDto::new).toList();
    }



}