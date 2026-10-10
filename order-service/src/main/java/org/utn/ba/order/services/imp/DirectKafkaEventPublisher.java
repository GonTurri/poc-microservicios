package org.utn.ba.order.services.imp;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.utn.ba.order.services.EventPublisher;

@Component("directKafkaEventPublisher")
@RequiredArgsConstructor
@Slf4j
public class DirectKafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(String topic, String key, Object event) {
        try {
            String payloadJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, key, payloadJson);
            log.info("Sent event directly to Kafka: topic={}, key={}", topic, key);
        } catch (Exception e) {
            log.error("Error sending directly to Kafka: {}", e.getMessage(), e);
            throw new RuntimeException("Error enviando evento directo a Kafka", e);
        }
    }
}
