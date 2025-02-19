package Hr.Mgr.domain.repository;

import Hr.Mgr.domain.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findTopByEmployeeIdOrderByAttendanceDateDesc(Long employeeId);

    List<Attendance> findAllByEmployeeId(Long employeeId);
}
