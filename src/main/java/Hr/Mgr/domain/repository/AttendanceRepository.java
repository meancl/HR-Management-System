package Hr.Mgr.domain.repository;

import Hr.Mgr.domain.dto.AttendanceResDto;
import Hr.Mgr.domain.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findTopByEmployeeIdOrderByAttendanceDateDesc(Long employeeId);
    List<Attendance> findAllByEmployeeId(Long employeeId);
    @EntityGraph(attributePaths = {"employee", "employee.department"})
    @Query("SELECT at FROM Attendance at " +
            "WHERE at.attendanceDate >= :startDate AND at.attendanceDate < :endDate")
    Page<Attendance> findByYearAndMonths(@Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate,
                                         Pageable pageable);

    @Query("SELECT MIN(at.attendanceDate) FROM Attendance at")
    Optional<LocalDate> findMinDate();
    @Query("SELECT MAX(at.attendanceDate) FROM Attendance at")
    Optional<LocalDate> findMaxDate();
    @Query("SELECT MAX(FUNCTION('month', at.attendanceDate)) " +
            "FROM Attendance at " +
            "WHERE at.attendanceDate >= :start AND at.attendanceDate < :end")
    Optional<Integer> findMaxMonthInYear(@Param("start") LocalDate start,
                               @Param("end") LocalDate end);
}
