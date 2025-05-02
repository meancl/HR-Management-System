package Hr.Mgr.domain.controller;

import Hr.Mgr.domain.dto.AttendanceReqDto;
import Hr.Mgr.domain.dto.AttendanceResDto;
import Hr.Mgr.domain.service.AttendanceService;
import Hr.Mgr.domain.service.AttendanceStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;


    @PostMapping
    public ResponseEntity<?> createAttendance(@RequestBody AttendanceReqDto attendanceReqDto) {


        AttendanceResDto attendance = attendanceService.createAttendance(attendanceReqDto);

        if (attendance == null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        else if(!attendance.getIsProcessed()){ // 처리중
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("처리중입니다...");
        }
        return ResponseEntity.ok().body(attendance);
    }




}
