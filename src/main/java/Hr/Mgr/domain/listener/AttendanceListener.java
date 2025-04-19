package Hr.Mgr.domain.listener;

import Hr.Mgr.domain.dto.AttendanceReqDto;
import Hr.Mgr.domain.entity.QuarterlyAttendanceStatistics;
import Hr.Mgr.domain.service.AttendanceService;
import Hr.Mgr.domain.service.AttendanceStatisticsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Profile("a_server")
@Component
@RequiredArgsConstructor
public class AttendanceListener {
    private Integer attendanceReqAccumSize = 0;
    private final AttendanceService attendanceService;
    private final AttendanceStatisticsService attendanceStatisticsService;
    private final ObjectMapper objectMapper;
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

    @Transactional
    @KafkaListener(topics = "${custom.kafka.topic.insert-attendance-statistics}", groupId = "${custom.kafka.group-id.insert-attendance-statistics}",  containerFactory = "insertAttendanceStatisticsKafkaListenerContainerFactory")
    public void createBatchAttendanceStatistics(List<QuarterlyAttendanceStatistics> attendanceStatisticsReqDtos) {

        try {
            List<MapSqlParameterSource> batchParams = new ArrayList<>();

            for (int batchedSize = 0; batchedSize < attendanceStatisticsReqDtos.size(); batchedSize++) {
                QuarterlyAttendanceStatistics attendanceReqDto = attendanceStatisticsReqDtos.get(batchedSize);
                String json = objectMapper.writeValueAsString(attendanceReqDto.getWeeklyWorkMinutes());
                LocalDateTime now = LocalDateTime.now();
                batchParams.add(new MapSqlParameterSource()
                        .addValue("avg_end_time", attendanceReqDto.getAvgEndTime())
                        .addValue("avg_overtime_minutes", attendanceReqDto.getAvgOvertimeMinutes())
                        .addValue("avg_start_time", attendanceReqDto.getAvgStartTime())
                        .addValue("avg_work_minutes", attendanceReqDto.getAvgWorkMinutes())
                        .addValue("department", attendanceReqDto.getDepartment())
                        .addValue("employee_id", attendanceReqDto.getEmployeeId())
                        .addValue("holiday_work_ratio", attendanceReqDto.getHolidayWorkRatio())
                        .addValue("late_count", attendanceReqDto.getLateCount())
                        .addValue("present_days", attendanceReqDto.getPresentDays())
                        .addValue("quarter", attendanceReqDto.getQuarter())
                        .addValue("present_days", attendanceReqDto.getPresentDays())
                        .addValue("weekly_work_minutes", json)
                        .addValue("year", attendanceReqDto.getYear())
                        .addValue("created_at", now)
                        .addValue("updated_at", now));

                if(batchedSize != 0 && batchedSize % 2000 == 0){
                    attendanceStatisticsService.batchInsertAttendancesStatistics(batchParams);
                    batchParams.clear();
                }
            }
            if(!batchParams.isEmpty())
                attendanceStatisticsService.batchInsertAttendancesStatistics(batchParams);
        }
        catch (Exception e){
            logger.warn("Kafka 에러입니다, message : {}", e);
        }
    }
}
