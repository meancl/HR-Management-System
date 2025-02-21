package Hr.Mgr.domain.serviceImpl;

import Hr.Mgr.domain.dto.PerformanceReviewDto;
import Hr.Mgr.domain.entity.Employee;
import Hr.Mgr.domain.entity.PerformanceReview;
import Hr.Mgr.domain.entity.ReviewScore;
import Hr.Mgr.domain.enums.ReviewStatus;
import Hr.Mgr.domain.exception.ResourceNotFoundException;
import Hr.Mgr.domain.repository.EmployeeRepository;
import Hr.Mgr.domain.repository.PerformanceReviewRepository;
import Hr.Mgr.domain.repository.ReviewScoreRepository;
import Hr.Mgr.domain.service.PerformanceReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PerformanceReviewServiceImpl implements PerformanceReviewService {

    private final PerformanceReviewRepository performanceReviewRepository;
    private final EmployeeRepository employeeRepository;
    private final ReviewScoreRepository reviewScoreRepository;


    @Override
    @Transactional
    public PerformanceReviewDto createReview(PerformanceReviewDto dto) {

        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        Employee reviewer = employeeRepository.findById(dto.getReviewerId())
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found"));

        PerformanceReview review = new PerformanceReview();
        review.setEmployee(employee);
        review.setReviewer(reviewer);
        review.setReviewDate(dto.getReviewDate());
        review.setComments(dto.getComments());
        review.setStatus(ReviewStatus.PENDING);

        PerformanceReview savedReview = performanceReviewRepository.save(review);

        if (dto.getScores() == null)
            throw new RuntimeException("no scores input");

        List<ReviewScore> scores = dto.getScores().stream().map(scoreDto -> {
            ReviewScore score = new ReviewScore();
            score.setPerformanceReview(savedReview);
            score.setCriteria(scoreDto.getCriteria());
            score.setScore(scoreDto.getScore());
            return score;
        }).collect(Collectors.toList());

        List<ReviewScore> reviewScores = reviewScoreRepository.saveAll(scores);

        // DTO 반환
        return new PerformanceReviewDto(savedReview, reviewScores);
    }

    @Override
    @Transactional(readOnly = true)
    public PerformanceReviewDto getReviewById(Long reviewId) {

        PerformanceReview review = performanceReviewRepository.findByIdWithEmployees(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        List<ReviewScore> byPerformanceReviewId = reviewScoreRepository.findByPerformanceReviewId(review.getId());

        return new PerformanceReviewDto(review, byPerformanceReviewId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerformanceReviewDto> getReviewsByEmployee(Long employeeId) {
        List<PerformanceReview> byEmployeeId = performanceReviewRepository.findByEmployeeId(employeeId);
        List<ReviewScore> allByReviewId = reviewScoreRepository.findAllByReviewId(byEmployeeId.stream().map(PerformanceReview::getId).toList());

        return byEmployeeId.stream()
                .map(review -> new PerformanceReviewDto(
                        review,
                        allByReviewId.stream()
                                .filter(score -> score.getPerformanceReview().getId().equals(review.getId()))
                                .toList()
                ))
                .toList();

    }

    @Override
    @Transactional(readOnly = true)
    public List<PerformanceReviewDto> getReviewsByReviewer(Long reviewerId) {
        List<PerformanceReview> byReviewerId = performanceReviewRepository.findByReviewerId(reviewerId);
        List<ReviewScore> allByReviewId = reviewScoreRepository.findAllByReviewId(byReviewerId.stream().map(PerformanceReview::getId).toList());


        // employee.getId로 lazy loading 한번 더 걸림..
        return byReviewerId.stream()
                .map(review -> new PerformanceReviewDto(
                        review,
                        allByReviewId.stream()
                                .filter(score -> score.getPerformanceReview().getId().equals(review.getId()))
                                .toList()
                ))
                .toList();
    }

    @Override
    @Transactional
    public PerformanceReviewDto updateReview(Long reviewId, PerformanceReviewDto dto) {
        PerformanceReview review = performanceReviewRepository.findByIdWithEmployees(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if(dto.getComments() != null) review.setComments(dto.getComments());
        if(dto.getReviewDate() != null) review.setReviewDate(dto.getReviewDate());

        List<ReviewScore> byPerformanceReviewId = reviewScoreRepository.findByPerformanceReviewId(reviewId);


        return new PerformanceReviewDto(performanceReviewRepository.save(review), byPerformanceReviewId);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        if (!performanceReviewRepository.existsById(reviewId))
            throw new ResourceNotFoundException("Review not found");

        performanceReviewRepository.deleteById(reviewId);
    }

    @Override
    @Transactional
    public void completeReview(Long reviewId) {
        PerformanceReview review = performanceReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        review.setStatus(ReviewStatus.COMPLETED);
        performanceReviewRepository.save(review);
    }
}
