package Hr.Mgr.domain.dto;

import Hr.Mgr.domain.entity.ReviewScore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewScoreDto {
    private String criteria;
    private Integer score;

    public ReviewScoreDto(){}
    public ReviewScoreDto(ReviewScore reviewScore) {
        this.criteria = reviewScore.getCriteria();
        this.score = reviewScore.getScore();
    }
}
