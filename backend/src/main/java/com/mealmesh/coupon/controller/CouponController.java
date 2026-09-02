package com.mealmesh.coupon.controller;

import com.mealmesh.coupon.dto.CouponCreateRequest;
import com.mealmesh.coupon.dto.CouponResponse;
import com.mealmesh.coupon.dto.CouponValidateRequest;
import com.mealmesh.coupon.dto.CouponValidateResponse;
import com.mealmesh.coupon.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CouponResponse> createCoupon(
            @Valid @RequestBody CouponCreateRequest request) {
        CouponResponse response = couponService.createCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/validate")
    public ResponseEntity<CouponValidateResponse> validateCoupon(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody CouponValidateRequest request) {
        CouponValidateResponse response = couponService.validateAndCalculateDiscount(
                userId, request.getCode(), request.getRestaurantId(), request.getSubtotal());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<List<CouponResponse>> getActiveCoupons(
            @RequestParam(required = false) UUID restaurantId,
            @AuthenticationPrincipal UUID userId) {
        List<CouponResponse> coupons = couponService.getActiveCoupons(restaurantId, userId);
        return ResponseEntity.ok(coupons);
    }

    @GetMapping("/{code}")
    public ResponseEntity<CouponResponse> getCouponByCode(@PathVariable String code) {
        CouponResponse coupon = couponService.getCouponByCode(code);
        return ResponseEntity.ok(coupon);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CouponResponse>> getAllCoupons(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CouponResponse> coupons = couponService.getAllCoupons(pageable);
        return ResponseEntity.ok(coupons);
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CouponResponse> deactivateCoupon(@PathVariable UUID id) {
        CouponResponse response = couponService.deactivateCoupon(id);
        return ResponseEntity.ok(response);
    }
}
