package com.mealmesh.order.dto;

import com.mealmesh.order.entity.Order;
import com.mealmesh.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private UUID id;
    private String orderNumber;
    private UUID customerId;
    private String customerName;
    private UUID restaurantId;
    private String restaurantName;
    private OrderStatus status;
    private String statusDisplayName;
    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal platformFee;
    private BigDecimal totalAmount;
    private String couponCode;
    private BigDecimal couponDiscount;
    private String paymentMethod;
    private String paymentStatus;
    private UUID paymentId;
    private String deliveryAddressSnapshot;
    private String deliveryInstructions;
    private java.time.Instant estimatedDeliveryTime;
    private java.time.Instant actualDeliveryTime;
    private String cancellationReason;
    private String rejectionReason;
    private List<OrderItemResponse> items;
    private List<OrderStatusHistoryResponse> statusHistory;
    private java.time.Instant createdAt;
    private java.time.Instant updatedAt;

    public static String getStatusDisplayName(OrderStatus status) {
        return switch (status) {
            case CREATED -> "Order Placed";
            case PAYMENT_PENDING -> "Payment Pending";
            case PAYMENT_CONFIRMED -> "Payment Confirmed";
            case RESTAURANT_PENDING -> "Restaurant Notified";
            case RESTAURANT_ACCEPTED -> "Restaurant Accepted";
            case PREPARING -> "Preparing";
            case READY_FOR_PICKUP -> "Ready for Pickup";
            case DELIVERY_PARTNER_ASSIGNED -> "Partner Assigned";
            case PICKED_UP -> "Picked Up";
            case OUT_FOR_DELIVERY -> "Out for Delivery";
            case DELIVERED -> "Delivered";
            case CANCELLED -> "Cancelled";
            case PAYMENT_FAILED -> "Payment Failed";
            case RESTAURANT_REJECTED -> "Rejected by Restaurant";
        };
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private UUID id;
        private UUID menuItemId;
        private String name;
        private String description;
        private java.math.BigDecimal unitPrice;
        private Integer quantity;
        private java.math.BigDecimal totalPrice;
        private String specialInstructions;
        private Boolean isVegetarian;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderStatusHistoryResponse {
        private UUID id;
        private String previousStatus;
        private String newStatus;
        private String changedBy;
        private String reason;
        private java.time.Instant createdAt;
    }
}