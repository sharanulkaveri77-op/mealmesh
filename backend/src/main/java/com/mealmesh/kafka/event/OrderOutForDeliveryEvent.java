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
public class OrderOutForDeliveryEvent {
    private UUID orderId;
    private String orderNumber;
    private UUID deliveryPartnerId;
    private String partnerName;
    private Instant startedAt;
    private String eventId = UUID.randomUUID().toString();
}