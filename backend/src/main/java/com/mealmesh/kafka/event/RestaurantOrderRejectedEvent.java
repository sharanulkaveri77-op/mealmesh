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
public class RestaurantOrderRejectedEvent {
    private UUID orderId;
    private String orderNumber;
    private UUID restaurantId;
    private String restaurantName;
    private String rejectionReason;
    private Instant rejectedAt;
    private String eventId = UUID.randomUUID().toString();
}