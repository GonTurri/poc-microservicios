package org.utn.ba.order.services;

public interface EventPublisher {
    void publish(String topic, String key, Object event);
}
