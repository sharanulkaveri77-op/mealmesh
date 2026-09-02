package com.mealmesh.payment.dto;

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
public class PaymentResponse {
    private String providerName;
    private String providerTransactionId;
    private String providerOrderId;
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    private String failureReason;
    private BigDecimal amount;
    private String currency;
    private String paymentUrl; // For redirect-based payments
    private String webhookUrl;
    private UUID orderId;
    private String idempotencyKey;
    private String metadata;
}