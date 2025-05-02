package Hr.Mgr.domain.controller;

import Hr.Mgr.domain.service.AttendanceStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/attendance-statistics")
@RequiredArgsConstructor
public class AttendanceStatisticsController {
    private final AttendanceStatisticsService attendanceStatisticsService;

    @PostMapping
    public ResponseEntity<?> createAttendanceStatistics() {
        try {
            attendanceStatisticsService.calculateAttendanceQuarterlyStatistics();
        }
        catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok().build();
    }
}
