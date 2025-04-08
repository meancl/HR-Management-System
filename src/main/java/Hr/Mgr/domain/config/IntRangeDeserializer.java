package Hr.Mgr.domain.config;

import Hr.Mgr.domain.serviceImpl.AttendanceStatisticsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

public class IntRangeDeserializer implements Deserializer<AttendanceStatisticsServiceImpl.IntRange> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public AttendanceStatisticsServiceImpl.IntRange deserialize(String topic, byte[] data) {
        try {
            return objectMapper.readValue(data, AttendanceStatisticsServiceImpl.IntRange.class);
        } catch (Exception e) {
            throw new SerializationException("Error deserializing IntRange", e);
        }
    }
}
