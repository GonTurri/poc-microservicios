package org.utn.ba.order.services.imp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.utn.ba.order.entities.models.outbox.OutboxMessage;
import org.utn.ba.order.entities.repositories.outbox.OutboxMessageRepository;
import org.utn.ba.order.services.EventPublisher;

import java.time.LocalDateTime;

@Component
@Primary
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisher implements EventPublisher {

    private final OutboxMessageRepository outboxMessageRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(String topic, String key, Object event) {
        try {
            String payloadJson = objectMapper.writeValueAsString(event);
            OutboxMessage message = OutboxMessage.builder()
                    .topic(topic)
                    .key(key)
                    .payload(payloadJson)
                    .creationTime(LocalDateTime.now())
                    .build();

            outboxMessageRepository.save(message);
            log.info("Saved event to Outbox table: topic={}, key={}", topic, key);
        } catch (JsonProcessingException e) {
            log.error("Error serializing event for outbox", e);
            throw new RuntimeException("Error fatal de serialización al guardar evento en outbox.", e);
        }
    }
}
