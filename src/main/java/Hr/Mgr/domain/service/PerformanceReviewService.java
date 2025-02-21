package Hr.Mgr.domain.service;

import Hr.Mgr.domain.dto.PerformanceReviewDto;

import java.util.List;

public interface PerformanceReviewService {
    PerformanceReviewDto createReview(PerformanceReviewDto dto);
    PerformanceReviewDto getReviewById(Long reviewId);
    List<PerformanceReviewDto> getReviewsByEmployee(Long employeeId);
    List<PerformanceReviewDto> getReviewsByReviewer(Long reviewerId);
    PerformanceReviewDto updateReview(Long reviewId, PerformanceReviewDto dto);
    void deleteReview(Long reviewId);
    void completeReview(Long reviewId);
}
