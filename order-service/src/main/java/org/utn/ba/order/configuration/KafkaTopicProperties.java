package org.utn.ba.order.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "kafka.topic")
@Getter
@Setter
public class KafkaTopicProperties {
  private String OrderConfirmations;
  private String clearCart;
}
