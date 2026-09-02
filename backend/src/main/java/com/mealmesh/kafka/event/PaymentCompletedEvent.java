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
public class PaymentCompletedEvent {
    private UUID paymentId;
    private UUID orderId;
    private String orderNumber;
    private UUID customerId;
    private String paymentMethod;
    private String providerTransactionId;
    private Instant completedAt;
    private String eventId = UUID.randomUUID().toString();
}