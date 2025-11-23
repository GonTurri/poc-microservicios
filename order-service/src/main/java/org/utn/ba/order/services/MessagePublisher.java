package org.utn.ba.order.services;

public interface MessagePublisher {
  void publish(String topic, String key, String payload);
}
