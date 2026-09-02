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
public class OrderStatusChangedEvent {
    private UUID orderId;
    private String orderNumber;
    private String previousStatus;
    private String newStatus;
    private String changedBy;
    private String reason;
    private Instant changedAt;
    private String eventId = UUID.randomUUID().toString();
}