package org.utn.ba.order.configuration;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "kafka.topic")
@Getter
public class KafkaTopicProperties {
  private String OrderConfirmations;
  private String clearCart;
}
