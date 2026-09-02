package com.mealmesh.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
    private UUID orderId;
    private String orderNumber;
    private UUID customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private UUID restaurantId;
    private String restaurantName;
    private String restaurantPhone;
    private String deliveryAddressSnapshot;
    private String deliveryInstructions;
    private BigDecimal totalAmount;
    private List<OrderItem> items;
    private Instant createdAt;
    private String eventId = UUID.randomUUID().toString();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private UUID menuItemId;
        private String name;
        private Integer quantity;
        private BigDecimal unitPrice;
        private Boolean isVegetarian;
    }
}