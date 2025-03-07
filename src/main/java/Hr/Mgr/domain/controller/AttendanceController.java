package Hr.Mgr.domain.controller;

import Hr.Mgr.domain.dto.AttendanceReqDto;
import Hr.Mgr.domain.dto.AttendanceResDto;
import Hr.Mgr.domain.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/new")
    @ResponseBody
    public ResponseEntity<AttendanceResDto> createAttendance(@RequestBody AttendanceReqDto attendanceReqDto) {


        AttendanceResDto attendance = attendanceService.createSingleAttendance(attendanceReqDto);

        if (attendance == null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(attendance);
        }
        return ResponseEntity.ok().body(attendance);
    }

}
