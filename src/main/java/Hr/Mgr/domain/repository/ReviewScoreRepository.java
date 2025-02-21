package Hr.Mgr.domain.repository;

import Hr.Mgr.domain.entity.ReviewScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewScoreRepository extends JpaRepository<ReviewScore, Long> {
    List<ReviewScore> findByPerformanceReviewId(Long reviewId); // 특정 평가의 점수 목록 조회

    @Query("SELECT rs FROM ReviewScore rs " +
            "JOIN FETCH rs.performanceReview " +
            "WHERE rs.performanceReview.id IN :reviewIds")
    List<ReviewScore> findAllByReviewId(@Param("reviewIds") List<Long> reviewIds);
}