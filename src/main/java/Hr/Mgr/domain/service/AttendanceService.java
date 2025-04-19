package Hr.Mgr.domain.service;

import Hr.Mgr.domain.dto.AttendanceReqDto;
import Hr.Mgr.domain.dto.AttendanceResDto;
import Hr.Mgr.domain.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.List;

public interface AttendanceService {
    AttendanceResDto createAttendance(AttendanceReqDto dto);
    AttendanceResDto findLatestAttendanceDtoByEmployeeId(Long employeeId);
    AttendanceResDto findAttendanceDtoById(Long attendanceId);
    Page<Attendance> findAttendanceEntitiesByYearAndMonths(Integer year, Integer startMonth, Integer endMonth, Pageable pageable);
    AttendanceResDto updateAttendance(Long attendanceId, AttendanceReqDto dto);
    void deleteAttendance(Long attendanceId);
    int getMinAttendanceYear();
    int getMaxAttendanceYear();
    int getMaxAttendanceMonth(int year);
    void batchInsertAttendances(List<MapSqlParameterSource> batchParams);


}
