package Hr.Mgr.domain.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.Serializer;
import org.xerial.snappy.Snappy;

public class SnappyValueSerializer implements Serializer<Object> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public byte[] serialize(String s, Object o) {
        try {
            objectMapper.registerModule(new JavaTimeModule());
            byte[] rawBytes = objectMapper.writeValueAsBytes(o);
            return Snappy.compress(rawBytes);
        } catch (Exception e) {
            throw new RuntimeException("Snappy 압축 실패", e);
        }
    }
}
