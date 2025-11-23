package org.utn.ba.order.services.imp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.utn.ba.order.services.MessagePublisher;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaMessagePublisher implements MessagePublisher {

  private final KafkaTemplate<String, String> kafkaTemplate;
  @Override
  public void publish(String topicName, String key, String payload) {
    CompletableFuture<SendResult<String, String>> future = kafkaTemplate
        .send(topicName, key, payload);

    future.whenComplete((result, ex) -> {
      if (ex == null) {
        log.info("Sent message to topic [" + topicName +
            "] with offset=[" + result.getRecordMetadata().offset() + "]");
      } else {

        log.error("Unable to send message to topic [" + topicName +
            "] due to : " + ex.getMessage(),ex);
      }
    });
  }
}
