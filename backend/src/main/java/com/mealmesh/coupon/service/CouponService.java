package com.mealmesh.coupon.service;

import com.mealmesh.common.exception.BadRequestException;
import com.mealmesh.common.exception.ResourceNotFoundException;
import com.mealmesh.coupon.dto.CouponCreateRequest;
import com.mealmesh.coupon.dto.CouponResponse;
import com.mealmesh.coupon.dto.CouponValidateResponse;
import com.mealmesh.coupon.entity.Coupon;
import com.mealmesh.coupon.entity.CouponUsage;
import com.mealmesh.coupon.entity.DiscountType;
import com.mealmesh.coupon.repository.CouponRepository;
import com.mealmesh.coupon.repository.CouponUsageRepository;
import com.mealmesh.order.entity.Order;
import com.mealmesh.order.repository.OrderRepository;
import com.mealmesh.outbox.service.OutboxService;
import com.mealmesh.user.entity.User;
import com.mealmesh.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OutboxService outboxService;

    @Transactional
    public CouponResponse createCoupon(CouponCreateRequest request) {
        String code = request.getCode().trim().toUpperCase();

        if (couponRepository.findByCodeIgnoreCase(code).isPresent()) {
            throw new BadRequestException("Coupon code already exists: " + code);
        }

        if (request.getValidUntil().isBefore(request.getValidFrom())) {
            throw new BadRequestException("Coupon validUntil must be after validFrom");
        }

        Coupon coupon = Coupon.builder()
                .code(code)
                .name(request.getName())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minimumOrderAmount(request.getMinimumOrderAmount() != null ? request.getMinimumOrderAmount() : BigDecimal.ZERO)
                .maximumDiscountAmount(request.getMaximumDiscountAmount())
                .usageLimit(request.getUsageLimit())
                .usageCount(0)
                .perUserUsageLimit(request.getPerUserUsageLimit() != null ? request.getPerUserUsageLimit() : 1)
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .isActive(true)
                .applicableRestaurants(request.getApplicableRestaurants())
                .applicableCuisines(request.getApplicableCuisines() != null ? request.getApplicableCuisines() : "[]")
                .newUsersOnly(request.getNewUsersOnly() != null ? request.getNewUsersOnly() : false)
                .build();

        coupon = couponRepository.save(coupon);
        log.info("Coupon created: id={}, code={}", coupon.getId(), coupon.getCode());
        return CouponResponse.fromEntity(coupon);
    }

    @Transactional(readOnly = true)
    public CouponValidateResponse validateAndCalculateDiscount(
            UUID userId, String couponCode, UUID restaurantId, BigDecimal subtotal) {

        if (couponCode == null || couponCode.isBlank()) {
            return CouponValidateResponse.builder()
                    .isValid(false)
                    .discountAmount(BigDecimal.ZERO)
                    .message("Coupon code cannot be empty")
                    .build();
        }

        String code = couponCode.trim().toUpperCase();
        Optional<Coupon> opt = couponRepository.findByCodeIgnoreCase(code);

        if (opt.isEmpty()) {
            return CouponValidateResponse.builder()
                    .isValid(false)
                    .discountAmount(BigDecimal.ZERO)
                    .message("Invalid coupon code: " + code)
                    .build();
        }

        Coupon coupon = opt.get();
        Instant now = Instant.now();

        if (!Boolean.TRUE.equals(coupon.getIsActive())) {
            return invalidResponse(coupon, "This coupon is no longer active");
        }

        if (now.isBefore(coupon.getValidFrom())) {
            return invalidResponse(coupon, "This coupon is not yet valid");
        }

        if (now.isAfter(coupon.getValidUntil())) {
            return invalidResponse(coupon, "This coupon has expired");
        }

        if (coupon.getUsageLimit() != null && coupon.getUsageCount() >= coupon.getUsageLimit()) {
            return invalidResponse(coupon, "This coupon has reached its total usage limit");
        }

        if (coupon.getMinimumOrderAmount() != null && subtotal.compareTo(coupon.getMinimumOrderAmount()) < 0) {
            return invalidResponse(coupon, "Minimum order amount of ₹" + coupon.getMinimumOrderAmount() + " required");
        }

        if (userId != null) {
            // Check per-user limit
            int perUserLimit = coupon.getPerUserUsageLimit() != null ? coupon.getPerUserUsageLimit() : 1;
            long userUsageCount = couponUsageRepository.countByCouponIdAndUserId(coupon.getId(), userId);
            if (userUsageCount >= perUserLimit) {
                return invalidResponse(coupon, "You have already used this coupon the maximum allowed times (" + perUserLimit + ")");
            }

            // Check new users only
            if (Boolean.TRUE.equals(coupon.getNewUsersOnly())) {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null && orderRepository.countByCustomer(user) > 0) {
                    return invalidResponse(coupon, "This coupon is only available for first-time customers");
                }
            }
        }

        // Check restaurant applicability
        if (restaurantId != null && coupon.getApplicableRestaurants() != null && !coupon.getApplicableRestaurants().isBlank()) {
            String appRest = coupon.getApplicableRestaurants();
            if (!appRest.contains(restaurantId.toString()) && !appRest.equals("{}") && !appRest.equals("[]")) {
                return invalidResponse(coupon, "This coupon is not applicable to the selected restaurant");
            }
        }

        // Calculate discount
        BigDecimal discount = calculateDiscount(coupon, subtotal);

        return CouponValidateResponse.builder()
                .isValid(true)
                .discountAmount(discount)
                .message("Coupon applied successfully! You saved ₹" + discount)
                .coupon(CouponResponse.fromEntity(coupon))
                .build();
    }

    public BigDecimal calculateDiscount(Coupon coupon, BigDecimal subtotal) {
        BigDecimal discount = BigDecimal.ZERO;

        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = subtotal.multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            if (coupon.getMaximumDiscountAmount() != null && discount.compareTo(coupon.getMaximumDiscountAmount()) > 0) {
                discount = coupon.getMaximumDiscountAmount();
            }
        } else if (coupon.getDiscountType() == DiscountType.FIXED_AMOUNT) {
            discount = coupon.getDiscountValue();
        }

        // Discount cannot exceed subtotal
        if (discount.compareTo(subtotal) > 0) {
            discount = subtotal;
        }

        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public void recordCouponUsage(String couponCode, UUID userId, UUID orderId, BigDecimal discountApplied) {
        if (couponCode == null || couponCode.isBlank() || discountApplied == null || discountApplied.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        Coupon coupon = couponRepository.findByCodeIgnoreCase(couponCode.trim().toUpperCase()).orElse(null);
        if (coupon == null) {
            log.warn("Cannot record coupon usage: coupon not found: {}", couponCode);
            return;
        }

        User user = userRepository.findById(userId).orElse(null);
        Order order = orderRepository.findById(orderId).orElse(null);

        if (user == null || order == null) {
            log.warn("Cannot record coupon usage: user or order not found");
            return;
        }

        // Increment usage count on coupon
        coupon.setUsageCount(coupon.getUsageCount() + 1);
        couponRepository.save(coupon);

        CouponUsage usage = CouponUsage.builder()
                .coupon(coupon)
                .user(user)
                .order(order)
                .discountApplied(discountApplied)
                .usedAt(Instant.now())
                .build();

        couponUsageRepository.save(usage);
        log.info("Coupon usage recorded: coupon={}, user={}, order={}, discount={}",
                coupon.getCode(), userId, orderId, discountApplied);

        // Outbox event
        outboxService.saveEvent("Coupon", coupon.getId(), "CouponUsedEvent", Map.of(
                "couponId", coupon.getId(),
                "couponCode", coupon.getCode(),
                "userId", userId,
                "orderId", orderId,
                "discountApplied", discountApplied,
                "usedAt", Instant.now().toString()
        ));
    }

    @Transactional(readOnly = true)
    public List<CouponResponse> getActiveCoupons(UUID restaurantId, UUID userId) {
        return couponRepository.findAvailableActiveCoupons(Instant.now()).stream()
                .filter(coupon -> {
                    if (restaurantId != null && coupon.getApplicableRestaurants() != null && !coupon.getApplicableRestaurants().isBlank()) {
                        String app = coupon.getApplicableRestaurants();
                        if (!app.contains(restaurantId.toString()) && !app.equals("{}") && !app.equals("[]")) {
                            return false;
                        }
                    }
                    if (userId != null && Boolean.TRUE.equals(coupon.getNewUsersOnly())) {
                        User user = userRepository.findById(userId).orElse(null);
                        if (user != null && orderRepository.countByCustomer(user) > 0) {
                            return false;
                        }
                    }
                    return true;
                })
                .map(CouponResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CouponResponse getCouponByCode(String code) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found: " + code));
        return CouponResponse.fromEntity(coupon);
    }

    @Transactional(readOnly = true)
    public Page<CouponResponse> getAllCoupons(Pageable pageable) {
        return couponRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(CouponResponse::fromEntity);
    }

    @Transactional
    public CouponResponse deactivateCoupon(UUID couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + couponId));
        coupon.setIsActive(false);
        coupon = couponRepository.save(coupon);
        return CouponResponse.fromEntity(coupon);
    }

    private CouponValidateResponse invalidResponse(Coupon coupon, String message) {
        return CouponValidateResponse.builder()
                .isValid(false)
                .discountAmount(BigDecimal.ZERO)
                .message(message)
                .coupon(CouponResponse.fromEntity(coupon))
                .build();
    }
}
