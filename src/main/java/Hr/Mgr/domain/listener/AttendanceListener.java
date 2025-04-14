package Hr.Mgr.domain.listener;

import Hr.Mgr.domain.dto.AttendanceReqDto;
import Hr.Mgr.domain.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Profile("a_server")
@Component
@RequiredArgsConstructor
public class AttendanceListener {
    private Integer attendanceReqAccumSize = 0;
    private final AttendanceService attendanceService;
    private final Logger logger = LoggerFactory.getLogger(AttendanceListener.class);
    @KafkaListener(topics = "${custom.kafka.topic.attendance}", groupId = "${custom.kafka.group-id.attendance}",  containerFactory = "attendanceBatchKafkaListenerContainerFactory")
    public void createBatchAttendances(List<AttendanceReqDto> attendanceReqDtos, Acknowledgment acknowledgment) {

        try {
            logger.info("listen to batch insert: {}", attendanceReqDtos.size());
            attendanceReqAccumSize += attendanceReqDtos.size();
            logger.info("batch accum size : {}", attendanceReqAccumSize);

            List<MapSqlParameterSource> batchParams = new ArrayList<>();

            for (AttendanceReqDto attendanceReqDto : attendanceReqDtos) {
                batchParams.add(new MapSqlParameterSource()
                        .addValue("employee_id", attendanceReqDto.getEmployeeId())
                        .addValue("attendance_date", attendanceReqDto.getAttendanceDate())
                        .addValue("check_in_time", attendanceReqDto.getCheckInTime())
                        .addValue("check_out_time", attendanceReqDto.getCheckOutTime())
                        .addValue("status", attendanceReqDto.getAttendanceStatus().toString())
                        .addValue("created_at", LocalDateTime.now())
                        .addValue("updated_at", LocalDateTime.now()));
            }

            attendanceService.batchInsertAttendances(batchParams);
            acknowledgment.acknowledge();
        }
        catch (Exception e){
            logger.warn("Kafka 에러입니다, message : {}", e);
        }
    }
}
