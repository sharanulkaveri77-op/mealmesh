package com.mealmesh.restaurant.controller;

import com.mealmesh.order.dto.OrderResponse;
import com.mealmesh.restaurant.service.RestaurantOperationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurant-portal")
@RequiredArgsConstructor
public class RestaurantOperationsController {

    private final RestaurantOperationsService operationsService;

    @GetMapping("/{restaurantId}/orders")
    public ResponseEntity<List<OrderResponse>> getLiveOrders(
            @PathVariable UUID restaurantId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(operationsService.getLiveOrders(restaurantId, userId));
    }

    @PostMapping("/{restaurantId}/orders/{orderId}/accept")
    public ResponseEntity<OrderResponse> acceptOrder(
            @PathVariable UUID restaurantId,
            @PathVariable UUID orderId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(operationsService.acceptOrder(restaurantId, orderId, userId));
    }

    @PostMapping("/{restaurantId}/orders/{orderId}/preparing")
    public ResponseEntity<OrderResponse> markPreparing(
            @PathVariable UUID restaurantId,
            @PathVariable UUID orderId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(operationsService.markOrderPreparing(restaurantId, orderId, userId));
    }

    @PostMapping("/{restaurantId}/orders/{orderId}/ready")
    public ResponseEntity<OrderResponse> markReady(
            @PathVariable UUID restaurantId,
            @PathVariable UUID orderId,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(operationsService.markOrderReady(restaurantId, orderId, userId));
    }

    @PostMapping("/{restaurantId}/orders/{orderId}/reject")
    public ResponseEntity<OrderResponse> rejectOrder(
            @PathVariable UUID restaurantId,
            @PathVariable UUID orderId,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal UUID userId) {
        String reason = body != null ? body.getOrDefault("reason", "Kitchen too busy") : "Kitchen too busy";
        return ResponseEntity.ok(operationsService.rejectOrder(restaurantId, orderId, userId, reason));
    }

    @PatchMapping("/{restaurantId}/items/{itemId}/stock")
    public ResponseEntity<Void> toggleItemStock(
            @PathVariable UUID restaurantId,
            @PathVariable UUID itemId,
            @RequestParam boolean available,
            @AuthenticationPrincipal UUID userId) {
        operationsService.toggleMenuItemStock(restaurantId, itemId, userId, available);
        return ResponseEntity.ok().build();
    }
}
