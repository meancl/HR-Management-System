package Hr.Mgr.domain.serviceImpl;

import Hr.Mgr.domain.dto.AttendanceReqDto;
import Hr.Mgr.domain.dto.AttendanceResDto;
import Hr.Mgr.domain.dto.EmployeeReqDto;
import Hr.Mgr.domain.enums.AttendanceStatus;
import Hr.Mgr.domain.service.AttendanceService;
import Hr.Mgr.domain.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AttendanceServiceImplTest {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;


    private AttendanceResDto attendance;
    private Long employeeId;

    @BeforeEach
    void beforeEach() {
        EmployeeReqDto employeeReqDto = new EmployeeReqDto();
        employeeReqDto.setName("민재");
        employeeReqDto.setEmail("sbe03253@naver.com");
        employeeReqDto.setPassword(bCryptPasswordEncoder.encode("passwd1234"));
        employeeReqDto.setAge(30);

        employeeId = employeeService.createEmployee(employeeReqDto);

        AttendanceReqDto  attendanceReqDto = new AttendanceReqDto();
        attendanceReqDto.setEmployeeId(employeeId);
        attendanceReqDto.setAttendanceDate(LocalDate.of(2025, 1, 25));
        attendanceReqDto.setCheckInTime(LocalTime.of(8, 58, 22));
        attendanceReqDto.setCheckOutTime(LocalTime.of(18, 7, 12));
        attendanceReqDto.setAttendanceStatus(AttendanceStatus.PRESENT);

        attendance = attendanceService.createAttendance(attendanceReqDto);
    }


    @Test
    void createAttendance() {
        assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
    }

    @Test
    void getLatestAttendanceByEmployee() {
        AttendanceResDto latestAttendanceByEmployee = attendanceService.findLatestAttendanceDtoByEmployeeId(employeeId);
        assertThat(latestAttendanceByEmployee.getStatus()).isNotEqualTo(AttendanceStatus.ABSENT);
    }

    @Test
    void getAttendanceById() {
        AttendanceResDto attendanceById = attendanceService.findAttendanceDtoById(attendance.getId());
        assertThat(attendanceById)
                .usingRecursiveComparison().isEqualTo(attendance);
    }

    @Test
    void getAttendancesByEmployee() {
        List<AttendanceResDto> attendancesByEmployee = attendanceService.findAttendanceDtosByEmployeeId(employeeId);
        assertThat(attendancesByEmployee.size()).isEqualTo(1);
    }

    @Test
    void updateAttendance() {
        AttendanceReqDto attendanceReqDto = new AttendanceReqDto();
        attendanceReqDto.setCheckInTime(LocalTime.of(9, 15));

        AttendanceResDto attendanceResDto = attendanceService.updateAttendance(attendance.getId(), attendanceReqDto);
        assertThat(attendance.getCheckInTime()).isNotEqualTo(attendanceResDto.getCheckInTime());
    }

    @Test
    void deleteAttendance() {
        attendanceService.deleteAttendance(attendance.getId());

        assertThrows(IllegalArgumentException.class,
                () -> attendanceService.findLatestAttendanceDtoByEmployeeId(employeeId));
    }

}