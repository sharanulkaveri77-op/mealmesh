package com.mealmesh.order.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class OrderRequest {
    @NotNull(message = "Address ID is required")
    private UUID addressId;

    @NotBlank(message = "Phone is required")
    @Size(min = 10, max = 20)
    private String phone;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    private String couponCode;

    private String deliveryInstructions;
}