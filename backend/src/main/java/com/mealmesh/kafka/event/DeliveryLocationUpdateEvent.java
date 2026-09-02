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
public class DeliveryLocationUpdateEvent {
    private UUID orderId;
    private UUID deliveryPartnerId;
    private java.math.BigDecimal latitude;
    private java.math.BigDecimal longitude;
    private java.math.BigDecimal accuracyMeters;
    private java.math.BigDecimal speedKmph;
    private java.math.BigDecimal headingDegrees;
    private Instant recordedAt;
    private String eventId = UUID.randomUUID().toString();
}