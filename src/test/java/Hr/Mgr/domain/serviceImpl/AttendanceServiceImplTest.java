package Hr.Mgr.domain.serviceImpl;


import Hr.Mgr.domain.dto.AttendanceReqDto;
import Hr.Mgr.domain.dto.AttendanceResDto;
import Hr.Mgr.domain.entity.Attendance;
import Hr.Mgr.domain.entity.Employee;
import Hr.Mgr.domain.enums.AttendanceStatus;
import Hr.Mgr.domain.repository.AttendanceRepository;
import Hr.Mgr.domain.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AttendanceServiceImplTest {

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    private AttendanceReqDto makeSampleDto() {
        AttendanceReqDto attendanceReqDto = new AttendanceReqDto(1L, LocalDate.now(),LocalTime.of(9, 0),
                LocalTime.of(18, 0), AttendanceStatus.PRESENT);

        return attendanceReqDto;
    }

    @Test
    void testCreateAttendance_inBatchMode() {
        // given
        AttendanceReqDto dto = makeSampleDto();
        attendanceService.isBatchModeActive = true;
        ReflectionTestUtils.setField(attendanceService, "attendanceTopic", "test-topic");

        when(redisTemplate.opsForValue().setIfAbsent(any(), any(), any()))
                .thenReturn(true);

        // when
        AttendanceResDto result = attendanceService.createAttendance(dto);

        // then
        verify(kafkaTemplate).send(anyString(), eq(dto));
        assertThat(result.getIsProcessed()).isFalse();

        verify(redisTemplate).delete(contains("attendance create lock"));
    }

    @Test
    void testCreateAttendance_directInsert() {
        // given
        AttendanceReqDto dto = makeSampleDto();
        attendanceService.isBatchModeActive = false;


        Employee employee = new Employee();
        ReflectionTestUtils.setField(employee, "id", 1L); // 또는 employee.setId(1L); 가능하면

        Attendance attendance = new Attendance(employee, dto.getAttendanceDate(), dto.getCheckInTime(), dto.getCheckOutTime(), dto.getAttendanceStatus());

        when(redisTemplate.opsForValue().setIfAbsent(any(), any(), any()))
                .thenReturn(true);
        when(employeeService.findEmployeeEntityById(dto.getEmployeeId()))
                .thenReturn(employee);
        when(attendanceRepository.save(any()))
                .thenReturn(attendance);

        // when
        AttendanceResDto result = attendanceService.createAttendance(dto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEmployeeId()).isEqualTo(dto.getEmployeeId());

        verify(attendanceRepository).save(any());
        verify(redisTemplate).delete(contains("attendance create lock"));
    }

    @Test
    void testCreateAttendance_withRedisLockFail() {
        // given
        AttendanceReqDto dto = makeSampleDto();
        when(redisTemplate.opsForValue().setIfAbsent(any(), any(), any()))
                .thenReturn(false);

        // when
        AttendanceResDto result = attendanceService.createAttendance(dto);

        // then
        assertThat(result).isNull();
        verify(attendanceRepository, never()).save(any());
    }
}
