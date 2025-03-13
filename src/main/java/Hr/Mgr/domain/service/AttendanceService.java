package Hr.Mgr.domain.service;

import Hr.Mgr.domain.dto.AttendanceReqDto;
import Hr.Mgr.domain.dto.AttendanceResDto;

import java.util.List;

public interface AttendanceService {
    AttendanceResDto createAttendance(AttendanceReqDto dto);
    AttendanceResDto findLatestAttendanceDtoByEmployeeId(Long employeeId);
    AttendanceResDto findAttendanceDtoById(Long attendanceId);
    List<AttendanceResDto> findAttendanceDtosByEmployeeId(Long employeeId);
    AttendanceResDto updateAttendance(Long attendanceId, AttendanceReqDto dto);
    void deleteAttendance(Long attendanceId);

}
