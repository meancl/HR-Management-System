package Hr.Mgr.domain.repository;

import Hr.Mgr.domain.entity.PerformanceReview;
import Hr.Mgr.domain.enums.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
    @Query("SELECT DISTINCT pr FROM PerformanceReview pr " +
            "JOIN FETCH pr.employee " +
            "JOIN FETCH pr.reviewer " +
            "WHERE pr.employee.id = :employeeId")
    List<PerformanceReview> findByEmployeeId(@Param("employeeId") Long employeeId); // 특정 직원의 평가 조회

    // employee가 null 이어도 가져오고 싶다면 LEFT JOIN FETCH 사용
    @Query("SELECT DISTINCT pr FROM PerformanceReview pr " +
            "JOIN FETCH pr.reviewer " +
            "JOIN FETCH pr.employee " +
            "WHERE pr.reviewer.id = :reviewerId")
    List<PerformanceReview> findByReviewerId(@Param("reviewerId") Long reviewerId); // 특정 평가자가 수행한 평가 조회

    @Query("SELECT DISTINCT pr FROM PerformanceReview pr " +
            "JOIN FETCH pr.employee " +
            "JOIN FETCH pr.reviewer " +
            "WHERE pr.id = :reviewId")
    Optional<PerformanceReview> findByIdWithEmployees(@Param("reviewId") Long reviewId);

}
