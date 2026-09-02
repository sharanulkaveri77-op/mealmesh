package com.mealmesh.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponValidateResponse {

    private boolean isValid;
    private BigDecimal discountAmount;
    private String message;
    private CouponResponse coupon;
}
