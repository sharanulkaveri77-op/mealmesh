package com.mealmesh.coupon.service;

import com.mealmesh.common.exception.BadRequestException;
import com.mealmesh.coupon.dto.CouponCreateRequest;
import com.mealmesh.coupon.dto.CouponResponse;
import com.mealmesh.coupon.dto.CouponValidateResponse;
import com.mealmesh.coupon.entity.Coupon;
import com.mealmesh.coupon.entity.DiscountType;
import com.mealmesh.coupon.repository.CouponRepository;
import com.mealmesh.coupon.repository.CouponUsageRepository;
import com.mealmesh.order.entity.Order;
import com.mealmesh.order.repository.OrderRepository;
import com.mealmesh.outbox.service.OutboxService;
import com.mealmesh.user.entity.User;
import com.mealmesh.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponUsageRepository couponUsageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private CouponService couponService;

    private UUID userId;
    private UUID restaurantId;
    private Coupon percentCoupon;
    private Coupon fixedCoupon;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        restaurantId = UUID.randomUUID();

        percentCoupon = Coupon.builder()
                .id(UUID.randomUUID())
                .code("MEAL20")
                .name("20% Off")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("20.00"))
                .minimumOrderAmount(new BigDecimal("200.00"))
                .maximumDiscountAmount(new BigDecimal("100.00"))
                .usageLimit(100)
                .usageCount(5)
                .perUserUsageLimit(2)
                .validFrom(Instant.now().minus(1, ChronoUnit.DAYS))
                .validUntil(Instant.now().plus(7, ChronoUnit.DAYS))
                .isActive(true)
                .newUsersOnly(false)
                .build();

        fixedCoupon = Coupon.builder()
                .id(UUID.randomUUID())
                .code("FLAT50")
                .name("Flat ₹50 Off")
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(new BigDecimal("50.00"))
                .minimumOrderAmount(new BigDecimal("150.00"))
                .usageLimit(50)
                .usageCount(0)
                .perUserUsageLimit(1)
                .validFrom(Instant.now().minus(1, ChronoUnit.DAYS))
                .validUntil(Instant.now().plus(7, ChronoUnit.DAYS))
                .isActive(true)
                .newUsersOnly(false)
                .build();
    }

    @Test
    @DisplayName("createCoupon should successfully save coupon in uppercase")
    void createCoupon_success() {
        // Arrange
        CouponCreateRequest request = CouponCreateRequest.builder()
                .code("welcome50")
                .name("Welcome 50")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("50.00"))
                .validFrom(Instant.now())
                .validUntil(Instant.now().plus(30, ChronoUnit.DAYS))
                .build();

        when(couponRepository.findByCodeIgnoreCase("WELCOME50")).thenReturn(Optional.empty());
        when(couponRepository.save(any(Coupon.class))).thenAnswer(inv -> {
            Coupon c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        // Act
        CouponResponse response = couponService.createCoupon(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo("WELCOME50");
        assertThat(response.getDiscountType()).isEqualTo(DiscountType.PERCENTAGE);
    }

    @Test
    @DisplayName("validateAndCalculateDiscount should apply percentage discount with maximum cap")
    void validate_percentageDiscountWithCap() {
        // Arrange
        when(couponRepository.findByCodeIgnoreCase("MEAL20")).thenReturn(Optional.of(percentCoupon));
        when(couponUsageRepository.countByCouponIdAndUserId(percentCoupon.getId(), userId)).thenReturn(0L);

        // Subtotal = 1000 -> 20% is 200, capped at 100
        BigDecimal subtotal = new BigDecimal("1000.00");

        // Act
        CouponValidateResponse response = couponService.validateAndCalculateDiscount(userId, "MEAL20", restaurantId, subtotal);

        // Assert
        assertThat(response.isValid()).isTrue();
        assertThat(response.getDiscountAmount()).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("validateAndCalculateDiscount should apply percentage discount below cap")
    void validate_percentageDiscountBelowCap() {
        // Arrange
        when(couponRepository.findByCodeIgnoreCase("MEAL20")).thenReturn(Optional.of(percentCoupon));
        when(couponUsageRepository.countByCouponIdAndUserId(percentCoupon.getId(), userId)).thenReturn(0L);

        // Subtotal = 300 -> 20% is 60.00
        BigDecimal subtotal = new BigDecimal("300.00");

        // Act
        CouponValidateResponse response = couponService.validateAndCalculateDiscount(userId, "MEAL20", restaurantId, subtotal);

        // Assert
        assertThat(response.isValid()).isTrue();
        assertThat(response.getDiscountAmount()).isEqualTo(new BigDecimal("60.00"));
    }

    @Test
    @DisplayName("validateAndCalculateDiscount should reject if minimum order amount not met")
    void validate_minimumOrderNotMet() {
        // Arrange
        when(couponRepository.findByCodeIgnoreCase("MEAL20")).thenReturn(Optional.of(percentCoupon));

        // Subtotal = 150 < 200 min order
        BigDecimal subtotal = new BigDecimal("150.00");

        // Act
        CouponValidateResponse response = couponService.validateAndCalculateDiscount(userId, "MEAL20", restaurantId, subtotal);

        // Assert
        assertThat(response.isValid()).isFalse();
        assertThat(response.getMessage()).contains("Minimum order amount");
    }

    @Test
    @DisplayName("validateAndCalculateDiscount should reject if user exceeded per-user limit")
    void validate_perUserLimitExceeded() {
        // Arrange
        when(couponRepository.findByCodeIgnoreCase("MEAL20")).thenReturn(Optional.of(percentCoupon));
        // User already used 2 times (perUserUsageLimit = 2)
        when(couponUsageRepository.countByCouponIdAndUserId(percentCoupon.getId(), userId)).thenReturn(2L);

        BigDecimal subtotal = new BigDecimal("500.00");

        // Act
        CouponValidateResponse response = couponService.validateAndCalculateDiscount(userId, "MEAL20", restaurantId, subtotal);

        // Assert
        assertThat(response.isValid()).isFalse();
        assertThat(response.getMessage()).contains("maximum allowed times");
    }

    @Test
    @DisplayName("validateAndCalculateDiscount should reject expired coupon")
    void validate_expiredCoupon() {
        // Arrange
        percentCoupon.setValidUntil(Instant.now().minus(1, ChronoUnit.DAYS));
        when(couponRepository.findByCodeIgnoreCase("MEAL20")).thenReturn(Optional.of(percentCoupon));

        BigDecimal subtotal = new BigDecimal("500.00");

        // Act
        CouponValidateResponse response = couponService.validateAndCalculateDiscount(userId, "MEAL20", restaurantId, subtotal);

        // Assert
        assertThat(response.isValid()).isFalse();
        assertThat(response.getMessage()).contains("expired");
    }

    @Test
    @DisplayName("recordCouponUsage should increment usage count and save usage record and outbox event")
    void recordCouponUsage_success() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        User user = User.builder().id(userId).name("Alice").build();
        Order order = Order.builder().id(orderId).orderNumber("ORD-001").build();

        when(couponRepository.findByCodeIgnoreCase("MEAL20")).thenReturn(Optional.of(percentCoupon));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        couponService.recordCouponUsage("MEAL20", userId, orderId, new BigDecimal("60.00"));

        // Assert
        assertThat(percentCoupon.getUsageCount()).isEqualTo(6);
        verify(couponRepository).save(percentCoupon);
        verify(couponUsageRepository).save(any());
        verify(outboxService).saveEvent(eq("Coupon"), eq(percentCoupon.getId()), eq("CouponUsedEvent"), any());
    }
}
