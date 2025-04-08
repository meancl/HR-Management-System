package Hr.Mgr.domain.controller;

import Hr.Mgr.domain.service.AttendanceStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/attendanceStatistics")
@RequiredArgsConstructor
public class AttendanceStatisticsController {
    private final AttendanceStatisticsService attendanceStatisticsService;

    @GetMapping("/create")
    @ResponseBody
    public ResponseEntity<?> createAttendanceStatistics() {
        try {
            attendanceStatisticsService.createAttendanceStatistics();
        }
        catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok().build();
    }
}
