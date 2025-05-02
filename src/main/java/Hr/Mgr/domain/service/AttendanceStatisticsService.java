package Hr.Mgr.domain.service;

import Hr.Mgr.domain.entity.QuarterlyAttendanceStatistics;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.List;

public interface AttendanceStatisticsService {
    void calculateAttendanceQuarterlyStatistics();
    void insertQuarterlyStatisticsBatch(List<QuarterlyAttendanceStatistics> batchParams) throws JsonProcessingException;
}
