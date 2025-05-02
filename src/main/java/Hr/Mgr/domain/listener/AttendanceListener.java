package Hr.Mgr.domain.listener;

import Hr.Mgr.domain.aspect.LogStartTime;
import Hr.Mgr.domain.aspect.MeasureExecutionTime;
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
            logger.info("batch accumulation size : {}", attendanceReqAccumSize);

            attendanceService.insertAttendanceBatch(attendanceReqDtos);
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
            final int BATCH_SIZE = 1000;
            int totalSize = attendanceStatisticsReqDtos.size();
            for (int i = 0; i < totalSize; i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, totalSize);
                List<QuarterlyAttendanceStatistics> batch = attendanceStatisticsReqDtos.subList(i, end);
                attendanceStatisticsService.insertQuarterlyStatisticsBatch(batch);
            }
        }
        catch (Exception e){
            logger.warn("Kafka 에러입니다, message : {}", e);
        }
    }
}
