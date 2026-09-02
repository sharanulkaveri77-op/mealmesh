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
public class OrderDeliveredEvent {
    private UUID orderId;
    private String orderNumber;
    private UUID customerId;
    private UUID deliveryPartnerId;
    private String partnerName;
    private Instant deliveredAt;
    private String eventId = UUID.randomUUID().toString();
}