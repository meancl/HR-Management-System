package Hr.Mgr.domain.serviceImpl;

import Hr.Mgr.domain.dto.AttendanceReqDto;
import Hr.Mgr.domain.dto.AttendanceResDto;
import Hr.Mgr.domain.entity.Attendance;
import Hr.Mgr.domain.entity.Employee;
import Hr.Mgr.domain.repository.AttendanceRepository;
import Hr.Mgr.domain.service.AttendanceService;
import Hr.Mgr.domain.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.Arrays.stream;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeService employeeService;

    @Override
    public AttendanceResDto createAttendance(AttendanceReqDto dto) {
        Employee employee = employeeService.findEmployeeEntityById(dto.getEmployeeId());
        Attendance attendance = new Attendance(
                employee, dto.getAttendanceDate(), dto.getCheckInTime(), dto.getCheckOutTime(), dto.getAttendanceStatus()
        );

        return new AttendanceResDto(attendanceRepository.save(attendance));
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResDto findLatestAttendanceDtoByEmployeeId(Long employeeId) {
        return attendanceRepository.findTopByEmployeeIdOrderByAttendanceDateDesc(employeeId)
                .map(AttendanceResDto::new).orElseThrow(() -> new IllegalArgumentException("No attendance found for employee"));
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResDto findAttendanceDtoById(Long attendanceId) {
        return attendanceRepository.findById(attendanceId)
                .map(AttendanceResDto::new).orElseThrow(() -> new IllegalArgumentException("No attendance found for employee"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResDto> findAttendanceDtosByEmployeeId(Long employeeId) {
        return attendanceRepository.findAllByEmployeeId(employeeId)
                .stream().map(AttendanceResDto::new).toList();
    }

    @Override
    public AttendanceResDto updateAttendance(Long attendanceId, AttendanceReqDto dto) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new IllegalArgumentException("Attendance not found"));
        if(dto.getAttendanceDate() != null) attendance.setAttendanceDate(dto.getAttendanceDate());
        if(dto.getCheckInTime() != null) attendance.setCheckInTime(dto.getCheckInTime());
        if(dto.getCheckOutTime() != null) attendance.setCheckOutTime(dto.getCheckOutTime());
        if(dto.getAttendanceStatus() != null) attendance.setStatus(dto.getAttendanceStatus());

        return new AttendanceResDto(attendanceRepository.save(attendance));
    }

    @Override
    public void deleteAttendance(Long attendanceId) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new IllegalArgumentException("Attendance not found"));
        attendanceRepository.delete(attendance);
    }
}
