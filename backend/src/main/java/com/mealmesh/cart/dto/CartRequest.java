package com.mealmesh.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CartRequest {
    @NotNull(message = "Menu item ID is required")
    private UUID menuItemId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity = 1;

    private String specialInstructions;
}