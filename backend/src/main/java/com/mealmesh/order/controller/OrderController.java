package com.mealmesh.order.controller;

import com.mealmesh.order.dto.OrderResponse;
import com.mealmesh.order.dto.OrderStatusUpdateRequest;
import com.mealmesh.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getCustomerOrders(
            @AuthenticationPrincipal UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(orderService.getCustomerOrders(userId, pageable));
    }

    @GetMapping("/active")
    public ResponseEntity<List<OrderResponse>> getActiveOrders(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(orderService.getCustomerActiveOrders(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrderByNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.getOrderByNumber(orderNumber));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable UUID id,
            @Valid @RequestBody OrderStatusUpdateRequest request,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request, userId));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable UUID id,
            @RequestParam String reason,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(orderService.cancelOrder(id, userId, reason));
    }

    @GetMapping("/restaurant")
    public ResponseEntity<Page<OrderResponse>> getRestaurantOrders(
            @RequestParam UUID restaurantId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(orderService.getRestaurantOrders(restaurantId, pageable));
    }

    @GetMapping("/restaurant/status")
    public ResponseEntity<List<OrderResponse>> getRestaurantOrdersByStatus(
            @RequestParam UUID restaurantId,
            @RequestParam List<com.mealmesh.order.entity.OrderStatus> statuses) {
        return ResponseEntity.ok(orderService.getRestaurantOrdersByStatus(restaurantId, statuses));
    }
}