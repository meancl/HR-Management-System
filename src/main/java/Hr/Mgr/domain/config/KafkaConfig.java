package Hr.Mgr.domain.config;

import Hr.Mgr.domain.dto.AttendanceReqDto;
import Hr.Mgr.domain.entity.QuarterlyAttendanceStatistics;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {
    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String resetConfig;
    @Value("${custom.kafka.group-id.attendance}")
    private String attendanceGroupId;
    @Value("${custom.kafka.group-id.insert-attendance-statistics}")
    private String insertAttendanceStatisticsGroupId;
    @Value("${custom.kafka.group-id.calculate-attendance-statistics}")
    private String calculateAttendanceStatisticsGroupId;
    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaServer;
    @Value("${spring.kafka.consumer.max-poll-records}")
    private Integer max_poll_records;
    @Value("${spring.kafka.listener.idle-between-polls}")
    private Long idle_between_polls;
    @Value("${spring.kafka.listener.poll-timeout}")
    private Long poll_timeout;


    // kafka uncompressed producer
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
    // kafka compressed producer
    @Bean
    public ProducerFactory<String, Object> compressedProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, SnappyValueSerializer.class );
//        config.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 5 * 1024 * 1024); // 5MB
        return new DefaultKafkaProducerFactory<>(config);
    }
    @Bean
    public KafkaTemplate<String, Object> compressedKafkaTemplate() {
        return new KafkaTemplate<>(compressedProducerFactory());
    }
    // batch attendance consumer
    @Bean
    public ConsumerFactory<String, AttendanceReqDto> attendanceBatchConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, attendanceGroupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, resetConfig);
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, max_poll_records);

        JsonDeserializer<AttendanceReqDto> deserializer = new JsonDeserializer<>(AttendanceReqDto.class);
        deserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AttendanceReqDto> attendanceBatchKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, AttendanceReqDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(attendanceBatchConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setBatchListener(true);
        // factory.getContainerProperties().setIdleBetweenPolls(idle_between_polls); // 폴링 후 0.3초 대기 후 다시 poll 실행
        factory.getContainerProperties().setPollTimeout(poll_timeout);

        return factory;
    }


    // list<QuarterlyAttendanceStatistics> consumer
    @Bean
    public ConsumerFactory<String, List<QuarterlyAttendanceStatistics>> insertAttendanceStatisticsConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, insertAttendanceStatisticsGroupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SnappyValueDeserializer.class);
//        config.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 5 * 1024 * 1024); // 5MB

        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, List<QuarterlyAttendanceStatistics>> insertAttendanceStatisticsKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, List<QuarterlyAttendanceStatistics>> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(insertAttendanceStatisticsConsumerFactory());
        return factory;
    }




}
