package Hr.Mgr.domain.repository;

import Hr.Mgr.domain.dto.AttendanceResDto;
import Hr.Mgr.domain.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findTopByEmployeeIdOrderByAttendanceDateDesc(Long employeeId);
    List<Attendance> findAllByEmployeeId(Long employeeId);
    @Query("SELECT at FROM Attendance at " +
            "WHERE FUNCTION('year', at.attendanceDate) = :year " +
            "AND FUNCTION('month', at.attendanceDate) BETWEEN :startMonth AND :endMonth")
    Page<Attendance> findByYearAndMonths(@Param("year") Integer year,
                                         @Param("startMonth") Integer startMonth,
                                         @Param("endMonth") Integer endMonth,
                                         Pageable pageable);

    @Query("SELECT MIN(FUNCTION('year', at.attendanceDate)) FROM Attendance at")
    Integer findMinYear();

    @Query("SELECT MAX(FUNCTION('year', at.attendanceDate)) FROM Attendance at")
    Integer findMaxYear();

    @Query("SELECT MAX(FUNCTION('month', at.attendanceDate)) FROM Attendance at" +
            " WHERE FUNCTION('year', at.attendanceDate) = :year ")
    Integer findMaxMonth(@Param("year") Integer year);
}
