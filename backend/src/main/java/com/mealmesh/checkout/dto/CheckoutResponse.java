package com.mealmesh.checkout.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponse {
    private UUID orderId;
    private String orderNumber;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String paymentStatus;
    private UUID paymentId;
    private String paymentUrl; // For redirecting to payment gateway
}