package Hr.Mgr.domain.controller;

import Hr.Mgr.domain.dto.*;
import Hr.Mgr.domain.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest({
        AttendanceController.class,
        AttendanceStatisticsController.class,
        EmployeeController.class,
        FileController.class,
        NoticeController.class,
        SalaryController.class
})
@WithMockUser // 전체 테스트에 인증된 사용자 컨텍스트 적용
public class ControllerTests {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean EmployeeService employeeService;
    @MockBean AttendanceService attendanceService;
    @MockBean AttendanceStatisticsService attendanceStatisticsService;
    @MockBean FileService fileService;
    @MockBean NoticeService noticeService;
    @MockBean NoticeCommentService noticeCommentService;
    @MockBean SalaryService salaryService;

    @Test
    @DisplayName("직원 등록 API")
    void registerEmployee() throws Exception {
        EmployeeReqDto req = new EmployeeReqDto();
        req.setName("홍길동");
        req.setEmail("hong@test.com");
        req.setAge(30);

        mockMvc.perform(MockMvcRequestBuilders.post("/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("출결 등록 API")
    void createAttendance() throws Exception {
        AttendanceReqDto req = new AttendanceReqDto();
        AttendanceResDto mockRes = new AttendanceResDto();
        mockRes.setIsProcessed(true);
        Mockito.when(attendanceService.createAttendance(Mockito.any())).thenReturn(mockRes);

        mockMvc.perform(MockMvcRequestBuilders.post("/attendances")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("공지 댓글 작성 API")
    void addNoticeComment() throws Exception {
        NoticeCommentDto dto = new NoticeCommentDto();
        dto.setAuthorId(1L);
        dto.setContent("좋은 공지입니다");

        mockMvc.perform(MockMvcRequestBuilders.post("/notices/1/comments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("급여 생성 API")
    void createSalary() throws Exception {
        SalaryReqDto dto = new SalaryReqDto();
        dto.setAmount(BigDecimal.valueOf(5000000));
        dto.setEmployeeId(1L);

        mockMvc.perform(MockMvcRequestBuilders.post("/salaries/employee/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("직원 목록 조회 API")
    void listEmployees() throws Exception {
        Mockito.when(employeeService.findAllEmployeeDtos(Mockito.any()))
                .thenReturn(Page.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/employees"))
                .andExpect(status().isOk());
    }
}
