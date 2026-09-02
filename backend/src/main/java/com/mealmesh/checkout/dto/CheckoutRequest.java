package com.mealmesh.checkout.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CheckoutRequest {
    @NotNull(message = "Address ID is required")
    private UUID addressId;

    @NotBlank(message = "Phone is required")
    @Size(min = 10, max = 20)
    private String phone;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // CARD, UPI, COD

    private String couponCode;

    private String deliveryInstructions;
}