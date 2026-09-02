package com.mealmesh.restaurant.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RestaurantRequest {
    @NotBlank(message = "Name is required")
    @Size(max = 255)
    private String name;

    @Size(max = 2000)
    private String description;

    @Size(max = 20)
    private String phone;

    @Email
    @Size(max = 255)
    private String email;

    private String imageUrl;

    private List<String> cuisineTypes;

    private Integer preparationTimeMinutes;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal minimumOrderAmount;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal deliveryFee;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal deliveryRadiusKm;

    private String openingTime;

    private String closingTime;

    private AddressRequest address;
}