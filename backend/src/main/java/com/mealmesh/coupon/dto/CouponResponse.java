package com.mealmesh.coupon.dto;

import com.mealmesh.coupon.entity.Coupon;
import com.mealmesh.coupon.entity.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponResponse {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minimumOrderAmount;
    private BigDecimal maximumDiscountAmount;
    private Integer usageLimit;
    private Integer usageCount;
    private Integer perUserUsageLimit;
    private Instant validFrom;
    private Instant validUntil;
    private Boolean isActive;
    private String applicableRestaurants;
    private String applicableCuisines;
    private Boolean newUsersOnly;
    private Instant createdAt;

    public static CouponResponse fromEntity(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .name(coupon.getName())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minimumOrderAmount(coupon.getMinimumOrderAmount())
                .maximumDiscountAmount(coupon.getMaximumDiscountAmount())
                .usageLimit(coupon.getUsageLimit())
                .usageCount(coupon.getUsageCount())
                .perUserUsageLimit(coupon.getPerUserUsageLimit())
                .validFrom(coupon.getValidFrom())
                .validUntil(coupon.getValidUntil())
                .isActive(coupon.getIsActive())
                .applicableRestaurants(coupon.getApplicableRestaurants())
                .applicableCuisines(coupon.getApplicableCuisines())
                .newUsersOnly(coupon.getNewUsersOnly())
                .createdAt(coupon.getCreatedAt())
                .build();
    }
}
