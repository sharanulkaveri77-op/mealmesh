package com.mealmesh.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private UUID userId;
    private String type; // ORDER_CREATED, PAYMENT_SUCCESS, ORDER_STATUS_CHANGED, etc.
    private String title;
    private String message;
    private String data; // JSON data
    private String priority; // HIGH, NORMAL, LOW
    private String relatedEntityType; // ORDER, PAYMENT, DELIVERY
    private UUID relatedEntityId;
    private Instant createdAt;
    private String eventId = UUID.randomUUID().toString();
}