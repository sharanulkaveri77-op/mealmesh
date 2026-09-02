package com.mealmesh.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPartnerAssignedEvent {
    private UUID orderId;
    private String orderNumber;
    private UUID deliveryPartnerId;
    private String partnerName;
    private String partnerPhone;
    private UUID assignmentId;
    private BigDecimal estimatedPickupDistanceKm;
    private Integer estimatedPickupTimeMinutes;
    private Instant assignedAt;
    private String eventId = UUID.randomUUID().toString();
}