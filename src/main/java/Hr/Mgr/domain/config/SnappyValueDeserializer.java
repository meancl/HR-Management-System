package Hr.Mgr.domain.config;

import Hr.Mgr.domain.entity.QuarterlyAttendanceStatistics;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.Deserializer;
import org.xerial.snappy.Snappy;

import java.util.List;

public class SnappyValueDeserializer implements Deserializer<List<QuarterlyAttendanceStatistics>> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<QuarterlyAttendanceStatistics> deserialize(String topic, byte[] data) {
        try {
            if (data == null || data.length == 0) return null;
            objectMapper.registerModule(new JavaTimeModule());
            byte[] uncompressed = Snappy.uncompress(data);
            return objectMapper.readValue(uncompressed, new TypeReference<List<QuarterlyAttendanceStatistics>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Snappy 역직렬화 실패", e);
        }
    }
}
