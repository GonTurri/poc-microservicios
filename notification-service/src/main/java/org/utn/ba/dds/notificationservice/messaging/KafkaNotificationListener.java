package org.utn.ba.dds.notificationservice.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.utn.ba.dds.notificationservice.model.NotificationMessage;
import org.utn.ba.dds.notificationservice.service.EmailService;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaNotificationListener {

    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topic.notifications:notifications}")
    private String notificationsTopic;

    @KafkaListener(topics = "#{'${kafka.topic.notifications:notifications}'}", containerFactory = "kafkaListenerContainerFactory")
    public void onMessage(String message) {
        try {
            NotificationMessage payload = objectMapper.readValue(message, NotificationMessage.class);
            emailService.sendEmail(payload.getTo(), payload.getSubject(), payload.getBody());
            log.info("Notificación procesada y email enviado a {}", payload.getTo());
        } catch (Exception e) {
            log.error("Error procesando mensaje de Kafka: {}", message, e);
        }
    }
}

