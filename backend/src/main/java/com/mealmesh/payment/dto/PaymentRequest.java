package com.mealmesh.payment.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentRequest {
    @NotNull
    private UUID orderId;

    @NotNull
    @DecimalMin(value = "0.01", inclusive = true)
    private BigDecimal amount;

    @NotNull
    @Size(min = 3, max = 3)
    private String currency = "INR";

    @NotNull
    @Size(min = 1, max = 50)
    private String paymentMethod; // CARD, UPI, COD

    private String idempotencyKey;
    private String customerEmail;
    private String customerPhone;
    private String returnUrl;
    private String webhookUrl;
}