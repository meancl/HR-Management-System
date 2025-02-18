package Hr.Mgr.domain.service;

import Hr.Mgr.domain.dto.AttendanceReqDto;
import Hr.Mgr.domain.dto.AttendanceResDto;
import Hr.Mgr.domain.enums.AttendanceStatus;

import java.util.List;

public interface AttendanceService {
    AttendanceResDto createAttendance(AttendanceReqDto dto);
    AttendanceResDto getLatestAttendanceByEmployee(Long employeeId);
    AttendanceResDto getAttendanceById(Long attendanceId);
    List<AttendanceResDto> getAttendancesByEmployee(Long employeeId);
    AttendanceResDto updateAttendance(Long attendanceId, AttendanceReqDto dto);
    void deleteAttendance(Long attendanceId);

}
