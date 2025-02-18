package Hr.Mgr.domain.repository;

import Hr.Mgr.domain.entity.Vacation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VacationRepository extends JpaRepository<Vacation, Long> {
    Optional<Vacation> findTopByEmployeeIdOrderByStartDateDesc(Long employeeId);

    List<Vacation> findByEmployeeId(Long employeeId);
}
