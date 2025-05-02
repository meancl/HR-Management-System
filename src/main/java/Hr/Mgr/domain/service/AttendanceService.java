package Hr.Mgr.domain.service;

import Hr.Mgr.domain.dto.AttendanceReqDto;
import Hr.Mgr.domain.dto.AttendanceResDto;
import Hr.Mgr.domain.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface AttendanceService {
    AttendanceResDto createAttendance(AttendanceReqDto dto);
    Page<Attendance> findAttendancePageByYearAndMonths(Integer year, Integer startMonth, Integer endMonth, Pageable pageable);
    Slice<Attendance> findAttendanceSliceByYearAndMonths(Long id, Integer year, Integer startMonth, Integer endMonth, Pageable pageable);
    int getMinAttendanceYear();
    int getMaxAttendanceYear();
    int getMaxAttendanceMonth(int year);
    void insertAttendanceBatch(List<AttendanceReqDto> attendanceReqDtos);
}
