package com.mealmesh.delivery.dto;

import com.mealmesh.delivery.entity.DeliveryAssignment;
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
public class DeliveryAssignmentResponse {

    private UUID id;
    private UUID orderId;
    private String orderNumber;
    private UUID restaurantId;
    private String restaurantName;
    private String restaurantPhone;
    private String customerName;
    private String customerPhone;
    private String deliveryAddress;
    private String status;
    private String rejectionReason;
    private Instant assignedAt;
    private Instant acceptedAt;
    private Instant pickedUpAt;
    private Instant deliveredAt;
    private Instant estimatedPickupTime;
    private Instant estimatedDeliveryTime;
    private BigDecimal earnings;

    public static DeliveryAssignmentResponse fromEntity(DeliveryAssignment assignment) {
        return DeliveryAssignmentResponse.builder()
                .id(assignment.getId())
                .orderId(assignment.getOrder().getId())
                .orderNumber(assignment.getOrder().getOrderNumber())
                .restaurantId(assignment.getOrder().getRestaurant().getId())
                .restaurantName(assignment.getOrder().getRestaurant().getName())
                .restaurantPhone(assignment.getOrder().getRestaurant().getPhone())
                .customerName(assignment.getOrder().getCustomer().getName())
                .customerPhone(assignment.getOrder().getCustomer().getPhone())
                .deliveryAddress(assignment.getOrder().getDeliveryAddressSnapshot())
                .status(assignment.getStatus())
                .rejectionReason(assignment.getRejectionReason())
                .assignedAt(assignment.getAssignedAt())
                .acceptedAt(assignment.getAcceptedAt())
                .pickedUpAt(assignment.getPickedUpAt())
                .deliveredAt(assignment.getDeliveredAt())
                .estimatedPickupTime(assignment.getEstimatedPickupTime())
                .estimatedDeliveryTime(assignment.getEstimatedDeliveryTime())
                .earnings(assignment.getEarnings())
                .build();
    }
}
