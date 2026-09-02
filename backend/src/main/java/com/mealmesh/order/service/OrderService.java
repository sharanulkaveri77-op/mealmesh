package com.mealmesh.order.service;

import com.mealmesh.common.exception.BadRequestException;
import com.mealmesh.common.exception.ResourceNotFoundException;
import com.mealmesh.order.dto.OrderRequest;
import com.mealmesh.order.dto.OrderResponse;
import com.mealmesh.order.dto.OrderStatusUpdateRequest;
import com.mealmesh.order.entity.Order;
import com.mealmesh.order.entity.OrderItem;
import com.mealmesh.order.entity.OrderStatus;
import com.mealmesh.order.entity.OrderStatusHistory;
import com.mealmesh.order.repository.OrderRepository;
import com.mealmesh.order.repository.OrderItemRepository;
import com.mealmesh.order.repository.OrderStatusHistoryRepository;
import com.mealmesh.restaurant.entity.Restaurant;
import com.mealmesh.restaurant.entity.RestaurantAddress;
import com.mealmesh.restaurant.repository.RestaurantAddressRepository;
import com.mealmesh.user.entity.User;
import com.mealmesh.user.repository.UserRepository;
import com.mealmesh.outbox.service.OutboxService;
import com.mealmesh.audit.annotation.Auditable;
import com.mealmesh.audit.entity.AuditAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final RestaurantAddressRepository addressRepository;
    private final UserRepository userRepository;
    private final OrderStateMachine stateMachine;
    private final OutboxService outboxService;

    @Transactional
    public OrderResponse createOrder(UUID userId, OrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        RestaurantAddress address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        // TODO: Get cart from cart service and create order
        // For now, this is a placeholder - actual creation happens in CheckoutService
        throw new BadRequestException("Use checkout endpoint to create orders");
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getCustomerOrders(UUID userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return orderRepository.findByCustomer(user, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getRestaurantOrders(UUID restaurantId, Pageable pageable) {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        return orderRepository.findByRestaurant(restaurant, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getRestaurantOrdersByStatus(UUID restaurantId, List<com.mealmesh.order.entity.OrderStatus> statuses) {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(restaurantId);
        return orderRepository.findByRestaurantAndStatusIn(restaurant, statuses)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    @Auditable(entityType = "Order", action = AuditAction.STATUS_CHANGE, entityIdParam = "orderId")
    public OrderResponse updateOrderStatus(UUID orderId, OrderStatusUpdateRequest request, UUID actorId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        OrderStatus from = order.getStatus();
        OrderStatus to = request.getNewStatus();

        OrderStateMachine.validateTransition(from, to);

        if (OrderStateMachine.isTerminal(from)) {
            throw new BadRequestException("Cannot change status of terminal order: " + from);
        }

        order.setPreviousStatus(from);
        order.setStatus(to);
        order.setUpdatedAt(Instant.now());

        // Set timestamps based on status
        switch (to) {
            case PAYMENT_CONFIRMED -> order.setPaymentStatus("COMPLETED");
            case RESTAURANT_ACCEPTED -> order.setEstimatedDeliveryTime(Instant.now().plusSeconds(order.getRestaurant().getPreparationTimeMinutes() * 60L + 30 * 60));
            case OUT_FOR_DELIVERY -> order.setActualDeliveryTime(Instant.now());
            case DELIVERED -> {
                order.setActualDeliveryTime(Instant.now());
                order.setPaymentStatus("COMPLETED");
            }
            case CANCELLED -> {
                order.setCancelledAt(Instant.now());
                // TODO: Trigger refund if payment was made
            }
            case PAYMENT_FAILED -> order.setPaymentStatus("FAILED");
        }

        order = orderRepository.save(order);

        // Record history
        User actor = userRepository.findById(actorId).orElse(null);
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .previousStatus(from)
                .newStatus(to)
                .changedBy(actor)
                .reason(request.getReason())
                .metadata("{}")
                .build();
        historyRepository.save(history);

        log.info("Order {} status changed: {} -> {} by user {}", order.getId(), from, to, actorId);

        // Save OrderStatusChangedEvent to outbox (atomically within this transaction)
        outboxService.saveEvent("Order", order.getId(), "OrderStatusChangedEvent",
                java.util.Map.of(
                        "orderId", order.getId(),
                        "orderNumber", order.getOrderNumber(),
                        "previousStatus", from.name(),
                        "newStatus", to.name(),
                        "actorId", actorId.toString(),
                        "timestamp", Instant.now().toString()
                ));

        return toResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(UUID orderId, UUID userId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!OrderStateMachine.canCancel(order.getStatus())) {
            throw new BadRequestException("Cannot cancel order in status: " + order.getStatus());
        }

        // Check ownership
        if (!order.getCustomer().getId().equals(userId)) {
            throw new BadRequestException("Not authorized to cancel this order");
        }

        return updateOrderStatus(orderId, OrderStatusUpdateRequest.builder()
                .newStatus(com.mealmesh.order.entity.OrderStatus.CANCELLED)
                .reason(reason)
                .build(), userId);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByPaymentId(UUID paymentId) {
        return orderRepository.findByPaymentId(paymentId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getCustomerActiveOrders(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return orderRepository.findByCustomerAndStatusIn(user, List.of(
                com.mealmesh.order.entity.OrderStatus.CREATED,
                com.mealmesh.order.entity.OrderStatus.PAYMENT_PENDING,
                com.mealmesh.order.entity.OrderStatus.PAYMENT_CONFIRMED,
                com.mealmesh.order.entity.OrderStatus.RESTAURANT_PENDING,
                com.mealmesh.order.entity.OrderStatus.RESTAURANT_ACCEPTED,
                com.mealmesh.order.entity.OrderStatus.PREPARING,
                com.mealmesh.order.entity.OrderStatus.READY_FOR_PICKUP,
                com.mealmesh.order.entity.OrderStatus.DELIVERY_PARTNER_ASSIGNED,
                com.mealmesh.order.entity.OrderStatus.PICKED_UP,
                com.mealmesh.order.entity.OrderStatus.OUT_FOR_DELIVERY
        )).stream().map(this::toResponse).toList();
    }

    private OrderResponse toResponse(Order order) {
        List<OrderResponse.OrderItemResponse> items = order.getOrderItems() != null
                ? order.getOrderItems().stream().map(this::toItemResponse).toList()
                : List.of();

        List<OrderResponse.OrderStatusHistoryResponse> history = historyRepository.findByOrderOrderByCreatedAtAsc(order)
                .stream().map(this::toHistoryResponse).toList();

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomer().getId())
                .customerName(order.getCustomer().getName())
                .restaurantId(order.getRestaurant().getId())
                .restaurantName(order.getRestaurant().getName())
                .status(order.getStatus())
                .statusDisplayName(OrderResponse.getStatusDisplayName(order.getStatus()))
                .subtotal(order.getSubtotal())
                .deliveryFee(order.getDeliveryFee())
                .taxAmount(order.getTaxAmount())
                .discountAmount(order.getDiscountAmount())
                .platformFee(order.getPlatformFee())
                .totalAmount(order.getTotalAmount())
                .couponCode(order.getCouponCode())
                .couponDiscount(order.getCouponDiscount())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .paymentId(order.getPaymentId())
                .deliveryAddressSnapshot(order.getDeliveryAddressSnapshot())
                .deliveryInstructions(order.getDeliveryInstructions())
                .estimatedDeliveryTime(order.getEstimatedDeliveryTime())
                .actualDeliveryTime(order.getActualDeliveryTime())
                .cancellationReason(order.getCancellationReason())
                .rejectionReason(order.getRejectionReason())
                .items(items)
                .statusHistory(history)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderResponse.OrderItemResponse toItemResponse(OrderItem item) {
        return OrderResponse.OrderItemResponse.builder()
                .id(item.getId())
                .menuItemId(item.getMenuItem().getId())
                .name(item.getMenuItemName())
                .description(item.getMenuItemDescription())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .totalPrice(item.getTotalPrice())
                .specialInstructions(item.getSpecialInstructions())
                .isVegetarian(item.getIsVegetarian())
                .build();
    }

    private OrderResponse.OrderStatusHistoryResponse toHistoryResponse(OrderStatusHistory h) {
        return OrderResponse.OrderStatusHistoryResponse.builder()
                .id(h.getId())
                .previousStatus(h.getPreviousStatus() != null ? h.getPreviousStatus().name() : null)
                .newStatus(h.getNewStatus().name())
                .changedBy(h.getChangedBy() != null ? h.getChangedBy().getName() : "System")
                .reason(h.getReason())
                .createdAt(h.getCreatedAt())
                .build();
    }
}