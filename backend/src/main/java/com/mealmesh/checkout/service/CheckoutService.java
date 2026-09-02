package com.mealmesh.checkout.service;

import com.mealmesh.cart.entity.Cart;
import com.mealmesh.cart.entity.CartItem;
import com.mealmesh.cart.repository.CartRepository;
import com.mealmesh.checkout.dto.CheckoutRequest;
import com.mealmesh.checkout.dto.CheckoutResponse;
import com.mealmesh.common.exception.BadRequestException;
import com.mealmesh.common.exception.ResourceNotFoundException;
import com.mealmesh.order.entity.Order;
import com.mealmesh.order.entity.OrderItem;
import com.mealmesh.order.entity.OrderStatus;
import com.mealmesh.order.repository.OrderRepository;
import com.mealmesh.order.repository.OrderItemRepository;
import com.mealmesh.payment.entity.Payment;
import com.mealmesh.payment.entity.PaymentStatus;
import com.mealmesh.payment.repository.PaymentRepository;
import com.mealmesh.restaurant.entity.Restaurant;
import com.mealmesh.restaurant.entity.RestaurantAddress;
import com.mealmesh.restaurant.repository.RestaurantAddressRepository;
import com.mealmesh.user.entity.User;
import com.mealmesh.user.repository.UserRepository;
import com.mealmesh.outbox.service.OutboxService;
import com.mealmesh.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final RestaurantAddressRepository addressRepository;
    private final UserRepository userRepository;
    private final OutboxService outboxService;
    private final CouponService couponService;

    @Transactional
    public CheckoutResponse checkout(UUID userId, CheckoutRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException("Cart is empty"));

        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        if (cart.getRestaurant() == null) {
            throw new BadRequestException("Restaurant not set in cart");
        }

        Restaurant restaurant = cart.getRestaurant();

        RestaurantAddress deliveryAddress = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Delivery address not found"));

        if (!deliveryAddress.getRestaurant().getId().equals(restaurant.getId()) && 
            !deliveryAddress.getUser().getId().equals(userId)) {
            // Allow user's own addresses and restaurant addresses
            throw new BadRequestException("Invalid delivery address");
        }

        BigDecimal subtotal = cart.getCartItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal deliveryFee = restaurant.getDeliveryFee() != null ? restaurant.getDeliveryFee() : BigDecimal.ZERO;
        BigDecimal minimumOrder = restaurant.getMinimumOrderAmount() != null ? restaurant.getMinimumOrderAmount() : BigDecimal.ZERO;

        if (subtotal.compareTo(minimumOrder) < 0) {
            throw new BadRequestException("Minimum order amount is ₹" + minimumOrder);
        }

        BigDecimal taxAmount = BigDecimal.ZERO; // Calculate if needed
        BigDecimal discountAmount = cart.getCouponDiscount() != null ? cart.getCouponDiscount() : BigDecimal.ZERO;
        BigDecimal platformFee = BigDecimal.ZERO;
        BigDecimal totalAmount = subtotal.add(deliveryFee).add(taxAmount).subtract(discountAmount).add(platformFee);

        // Create order
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .customer(user)
                .restaurant(restaurant)
                .status(OrderStatus.CREATED)
                .subtotal(subtotal)
                .deliveryFee(deliveryFee)
                .taxAmount(taxAmount)
                .discountAmount(discountAmount)
                .platformFee(platformFee)
                .totalAmount(totalAmount)
                .couponCode(cart.getCouponCode())
                .couponDiscount(discountAmount)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus("PENDING")
                .deliveryAddressSnapshot(addressToJson(deliveryAddress))
                .deliveryInstructions(request.getDeliveryInstructions())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        order = orderRepository.save(order);

        // Create order items
        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .menuItem(cartItem.getMenuItem())
                    .menuItemName(cartItem.getMenuItem().getName())
                    .menuItemDescription(cartItem.getMenuItem().getDescription())
                    .unitPrice(cartItem.getUnitPrice())
                    .quantity(cartItem.getQuantity())
                    .totalPrice(cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                    .specialInstructions(cartItem.getSpecialInstructions())
                    .isVegetarian(cartItem.getMenuItem().getIsVegetarian())
                    .build();
            orderItemRepository.save(orderItem);
        }

        // Create payment record
        Payment payment = Payment.builder()
                .order(order)
                .paymentIdempotencyKey(order.getId().toString() + "-" + System.currentTimeMillis())
                .amount(totalAmount)
                .currency("INR")
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .initiatedAt(Instant.now())
                .build();

        payment = paymentRepository.save(payment);

        // Update order with payment reference
        order.setPayment(payment);
        order.setPaymentStatus("PENDING");
        order.setStatus(OrderStatus.PAYMENT_PENDING);
        orderRepository.save(order);

        // Clear cart
        // cart.getCartItems().clear(); // Don't clear here, let payment success clear it
        // cart.setRestaurant(null);
        // cart.setCouponCode(null);
        // cart.setCouponDiscount(BigDecimal.ZERO);
        // Record coupon usage if coupon was applied
        if (cart.getCouponCode() != null && !cart.getCouponCode().isBlank() &&
                cart.getCouponDiscount() != null && cart.getCouponDiscount().compareTo(BigDecimal.ZERO) > 0) {
            couponService.recordCouponUsage(cart.getCouponCode(), user.getId(), order.getId(), cart.getCouponDiscount());
        }

        // Save OrderCreatedEvent to outbox (atomically within this transaction)
        outboxService.saveEvent("Order", order.getId(), "OrderCreatedEvent",
                java.util.Map.of(
                        "orderId", order.getId(),
                        "orderNumber", order.getOrderNumber(),
                        "customerId", user.getId(),
                        "restaurantId", restaurant.getId(),
                        "totalAmount", totalAmount,
                        "paymentMethod", request.getPaymentMethod(),
                        "createdAt", order.getCreatedAt().toString()
                ));

        log.info("Order created: {} for user: {}", order.getOrderNumber(), user.getEmail());

        return CheckoutResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .totalAmount(totalAmount)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus("PENDING")
                .paymentId(payment.getId())
                .paymentUrl(null) // Will be set by payment gateway integration
                .build();
    }

    private String generateOrderNumber() {
        return "ORD-" + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + String.format("%04d", (int)(Math.random() * 10000));
    }

    private String addressToJson(RestaurantAddress address) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(java.util.Map.of(
                    "streetAddress", address.getStreetAddress(),
                    "city", address.getCity(),
                    "state", address.getState(),
                    "postalCode", address.getPostalCode(),
                    "country", address.getCountry(),
                    "latitude", address.getLatitude(),
                    "longitude", address.getLongitude()
            ));
        } catch (Exception e) {
            return "{}";
        }
    }
}