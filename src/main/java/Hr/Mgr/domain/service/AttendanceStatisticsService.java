package Hr.Mgr.domain.service;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.List;

public interface AttendanceStatisticsService {
    void createAttendanceStatistics();
    public void batchInsertAttendancesStatistics(List<MapSqlParameterSource> batchParams);
}
