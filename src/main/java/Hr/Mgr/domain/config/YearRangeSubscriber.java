package Hr.Mgr.domain.config;

import Hr.Mgr.domain.serviceImpl.AttendanceStatisticsServiceImpl;
import Hr.Mgr.domain.serviceImpl.AttendanceStatisticsServiceImpl.IntRange;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import static Hr.Mgr.domain.serviceImpl.AttendanceStatisticsServiceImpl.ATTENDANCE_STATISTICS_YEAR_KEY;

@Component
@RequiredArgsConstructor
public class YearRangeSubscriber implements MessageListener {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RedisTemplate<String, String> redisTemplate;
    private final AttendanceStatisticsServiceImpl attendanceStatisticsService;
    private Logger logger = LoggerFactory.getLogger(MessageListener.class);
    int maxConcurrency = 3;
    private final Semaphore semaphore = new Semaphore(maxConcurrency);
    private final ExecutorService executorService = Executors.newFixedThreadPool(maxConcurrency);
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
        String body = new String(message.getBody(), StandardCharsets.UTF_8);

        try {
            String yearRangeJsonData;
            while(true) {
                try {
                    semaphore.acquire();

                    yearRangeJsonData = redisTemplate.opsForList().leftPop(ATTENDANCE_STATISTICS_YEAR_KEY);
                    logger.info("{} 선점!!", yearRangeJsonData);
                    if (yearRangeJsonData == null) {
                        semaphore.release();
                        break;
                    }
                    IntRange yearRange = objectMapper.readValue(yearRangeJsonData, new TypeReference<IntRange>() {
                    });
                    executorService.submit(() -> {
                        try {
                            attendanceStatisticsService.calculateAttendanceStatistics(yearRange);
                        } catch (Exception e) {
                            logger.error("❌ 통계 처리 중 예외", e);
                        } finally {
                            semaphore.release();
                        }
                    });
                } catch (Exception e) {
                    logger.error("❌ 전체 루프 예외", e);
                    // 예외 발생 시 점유했던 세마포어는 반납해야 함
                    semaphore.release();
                }
            }

        } catch (Exception e) {
            logger.error("❌ 메시지 파싱 실패: " + e.getMessage());
        }
    }
}