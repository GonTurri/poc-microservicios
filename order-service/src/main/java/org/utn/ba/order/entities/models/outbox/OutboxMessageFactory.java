package org.utn.ba.order.entities.models.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.utn.ba.events.ClearCartEvent;
import org.utn.ba.events.OrderConfirmationEvent;
import org.utn.ba.order.configuration.KafkaTopicProperties;
import org.utn.ba.order.entities.models.Order;
import org.utn.ba.order.mappers.OrderConfirmationEventMapper;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OutboxMessageFactory {

  private final ObjectMapper objectMapper;
  private final KafkaTopicProperties topicProperties;

  public OutboxMessage createOrderConfirmationMessage(Order order) throws JsonProcessingException {
    OrderConfirmationEvent event = OrderConfirmationEventMapper.fromOrder(order);
    String payloadJson = objectMapper.writeValueAsString(event);

    return OutboxMessage.builder()
        .topic(topicProperties.getOrderConfirmations())
        .payload(payloadJson)
        .creationTime(LocalDateTime.now())
        .build();
  }

  public OutboxMessage createClearCartMessage(String userId) throws JsonProcessingException {
    ClearCartEvent event = new ClearCartEvent(userId);
    String payloadJson = objectMapper.writeValueAsString(event);

    return OutboxMessage.builder()
        .topic(topicProperties.getClearCart())
        .payload(payloadJson)
        .creationTime(LocalDateTime.now())
        .build();
  }
}
