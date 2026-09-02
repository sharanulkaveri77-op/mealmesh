package com.mealmesh.menu.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class MenuItemRequest {
    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    @NotBlank(message = "Name is required")
    @Size(max = 255)
    private String name;

    @Size(max = 2000)
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", inclusive = true)
    private BigDecimal price;

    private BigDecimal originalPrice;

    private String imageUrl;

    private Boolean isVegetarian = false;

    private Boolean isVegan = false;

    private Boolean isGlutenFree = false;

    @Min(0)
    @Max(5)
    private Integer spiceLevel = 0;

    @Min(1)
    private Integer preparationTimeMinutes = 15;

    private Boolean isAvailable = true;

    private Boolean isFeatured = false;

    private Integer displayOrder = 0;

    private List<String> tags;

    private String nutritionalInfo;
}