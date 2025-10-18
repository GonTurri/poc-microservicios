package org.utn.ba.dds.notificationservice.model;

import lombok.Data;

@Data
public class NotificationMessage {
    private String to;
    private String subject;
    private String body;
}

