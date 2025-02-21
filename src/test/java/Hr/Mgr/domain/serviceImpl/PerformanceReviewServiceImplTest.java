package Hr.Mgr.domain.serviceImpl;

import Hr.Mgr.domain.dto.PerformanceReviewDto;
import Hr.Mgr.domain.dto.ReviewScoreDto;
import Hr.Mgr.domain.entity.Employee;
import Hr.Mgr.domain.entity.PerformanceReview;
import Hr.Mgr.domain.entity.ReviewScore;
import Hr.Mgr.domain.enums.ReviewStatus;
import Hr.Mgr.domain.repository.EmployeeRepository;
import Hr.Mgr.domain.repository.PerformanceReviewRepository;
import Hr.Mgr.domain.repository.ReviewScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class PerformanceReviewServiceImplTest {
    @Mock
    private PerformanceReviewRepository performanceReviewRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ReviewScoreRepository reviewScoreRepository;

    @InjectMocks
    private PerformanceReviewServiceImpl performanceReviewService;

    private Employee employee;
    private Employee reviewer;
    private PerformanceReview review;
    private ReviewScore reviewScore;

    private LocalDate localDate = LocalDate.now();

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setName("John Doe");
        employee.setEmail("Doe@naver.com");
        employee.setHashedPwd("passwd1234");
        employee.setAge(30);

        reviewer = new Employee();
        reviewer.setId(2L);
        reviewer.setName("Jane Smith");
        reviewer.setEmail("Smith@naver.com");
        reviewer.setHashedPwd("passwd2345");
        reviewer.setAge(32);

        review = new PerformanceReview();
        review.setId(1L);
        review.setEmployee(employee);
        review.setReviewer(reviewer);
        review.setReviewDate(localDate);
        review.setComments("Great work");
        review.setStatus(ReviewStatus.PENDING);

        reviewScore = new ReviewScore();
        reviewScore.setId(1L);
        reviewScore.setPerformanceReview(review);
        reviewScore.setCriteria("Work Quality");
        reviewScore.setScore(5);
    }

    @Test
    void createReview() {

        ReviewScoreDto reviewScoreDto = new ReviewScoreDto();
        reviewScoreDto.setScore(5);
        reviewScoreDto.setCriteria("Work Quality");

        PerformanceReviewDto dto = new PerformanceReviewDto();
        dto.setEmployeeId(employee.getId());
        dto.setReviewerId(reviewer.getId());
        dto.setReviewDate(localDate);
        dto.setComments("Great work");
        dto.setScores(List.of(reviewScoreDto));

        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(employeeRepository.findById(reviewer.getId())).thenReturn(Optional.of(reviewer));
        when(performanceReviewRepository.save(any(PerformanceReview.class))).thenReturn(review);
        when(reviewScoreRepository.saveAll(any())).thenReturn(List.of(reviewScore));

        PerformanceReviewDto result = performanceReviewService.createReview(dto);

        assertNotNull(result);
        assertEquals("Great work", result.getComments());
        verify(performanceReviewRepository, times(1)).save(any(PerformanceReview.class));
    }

    @Test
    void getReviewById() {
        when(performanceReviewRepository.findByIdWithEmployees(review.getId())).thenReturn(Optional.of(review));
        when(reviewScoreRepository.findByPerformanceReviewId(review.getId())).thenReturn(List.of(reviewScore));

        PerformanceReviewDto result = performanceReviewService.getReviewById(review.getId());

        assertNotNull(result);
        assertEquals(review.getId(), result.getId());
        verify(performanceReviewRepository, times(1)).findByIdWithEmployees(review.getId());
    }

    @Test
    void getReviewsByEmployee() {
        when(performanceReviewRepository.findByEmployeeId(employee.getId())).thenReturn(List.of(review));
        when(reviewScoreRepository.findAllByReviewId(any())).thenReturn(List.of(reviewScore));

        List<PerformanceReviewDto> results = performanceReviewService.getReviewsByEmployee(employee.getId());

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
    }

    @Test
    void getReviewsByReviewer() {
        when(performanceReviewRepository.findByReviewerId(reviewer.getId())).thenReturn(List.of(review));
        when(reviewScoreRepository.findAllByReviewId(any())).thenReturn(List.of(reviewScore));

        List<PerformanceReviewDto> results = performanceReviewService.getReviewsByReviewer(reviewer.getId());

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
    }

    @Test
    void updateReview() {
        when(performanceReviewRepository.findByIdWithEmployees(review.getId())).thenReturn(Optional.of(review));
        when(performanceReviewRepository.save(any(PerformanceReview.class))).thenReturn(review);

        PerformanceReviewDto dto = new PerformanceReviewDto();
        dto.setComments("Updated comments");

        PerformanceReviewDto updatedReview = performanceReviewService.updateReview(review.getId(), dto);

        assertNotNull(updatedReview);
        assertEquals("Updated comments", updatedReview.getComments());
        verify(performanceReviewRepository, times(1)).save(any(PerformanceReview.class));
    }

    @Test
    void deleteReview() {
        when(performanceReviewRepository.existsById(review.getId())).thenReturn(true);
        doNothing().when(performanceReviewRepository).deleteById(review.getId());

        assertDoesNotThrow(() -> performanceReviewService.deleteReview(review.getId()));
        verify(performanceReviewRepository, times(1)).deleteById(review.getId());
    }

    @Test
    void completeReview() {
        when(performanceReviewRepository.findById(review.getId())).thenReturn(Optional.of(review));
        when(performanceReviewRepository.save(any(PerformanceReview.class))).thenReturn(review);

        assertDoesNotThrow(() -> performanceReviewService.completeReview(review.getId()));
        verify(performanceReviewRepository, times(1)).save(review);
    }
}