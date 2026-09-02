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
public class OrderPreparingEvent {
    private UUID orderId;
    private String orderNumber;
    private UUID restaurantId;
    private String restaurantName;
    private Instant startedAt;
    private String eventId = UUID.randomUUID().toString();
}