package com.mealmesh.coupon.dto;

import com.mealmesh.coupon.entity.DiscountType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponCreateRequest {

    @NotBlank(message = "Coupon code is required")
    @Size(min = 3, max = 50, message = "Coupon code must be between 3 and 50 characters")
    private String code;

    @NotBlank(message = "Coupon name is required")
    @Size(max = 255, message = "Coupon name cannot exceed 255 characters")
    private String name;

    private String description;

    @NotNull(message = "Discount type is required")
    private DiscountType discountType;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.00", message = "Minimum order amount must be non-negative")
    private BigDecimal minimumOrderAmount;

    private BigDecimal maximumDiscountAmount;

    @Min(value = 1, message = "Usage limit must be at least 1")
    private Integer usageLimit;

    @Min(value = 1, message = "Per-user usage limit must be at least 1")
    private Integer perUserUsageLimit;

    @NotNull(message = "Valid from timestamp is required")
    private Instant validFrom;

    @NotNull(message = "Valid until timestamp is required")
    private Instant validUntil;

    private String applicableRestaurants;
    private String applicableCuisines;
    private Boolean newUsersOnly;
}
