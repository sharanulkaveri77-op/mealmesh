package com.mealmesh.cart.controller;

import com.mealmesh.cart.dto.CartItemResponse;
import com.mealmesh.cart.dto.CartRequest;
import com.mealmesh.cart.dto.CartResponse;
import com.mealmesh.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<CartItemResponse> addItem(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody CartRequest request) {
        return ResponseEntity.ok(cartService.addItem(userId, request));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartItemResponse> updateQuantity(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID itemId,
            @RequestParam Integer quantity) {
        CartItemResponse response = cartService.updateQuantity(userId, itemId, quantity);
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID itemId) {
        cartService.removeItem(userId, itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UUID userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/coupon")
    public ResponseEntity<CartResponse> applyCoupon(
            @AuthenticationPrincipal UUID userId,
            @RequestParam String code) {
        return ResponseEntity.ok(cartService.applyCoupon(userId, code));
    }

    @DeleteMapping("/coupon")
    public ResponseEntity<CartResponse> removeCoupon(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(cartService.removeCoupon(userId));
    }
}