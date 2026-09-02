package com.mealmesh.menu.controller;

import com.mealmesh.menu.dto.MenuCategoryRequest;
import com.mealmesh.menu.dto.MenuCategoryResponse;
import com.mealmesh.menu.dto.MenuItemRequest;
import com.mealmesh.menu.dto.MenuItemResponse;
import com.mealmesh.menu.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurants/{restaurantId}/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    // Category endpoints
    @PostMapping("/categories")
    public ResponseEntity<MenuCategoryResponse> createCategory(
            @PathVariable UUID restaurantId,
            @Valid @RequestBody MenuCategoryRequest request,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(menuService.createCategory(restaurantId, userId, request));
    }

    @PutMapping("/categories/{categoryId}")
    public ResponseEntity<MenuCategoryResponse> updateCategory(
            @PathVariable UUID restaurantId,
            @PathVariable UUID categoryId,
            @Valid @RequestBody MenuCategoryRequest request,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(menuService.updateCategory(restaurantId, categoryId, userId, request));
    }

    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable UUID restaurantId,
            @PathVariable UUID categoryId,
            @AuthenticationPrincipal UUID userId) {
        menuService.deleteCategory(restaurantId, categoryId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categories")
    public ResponseEntity<List<MenuCategoryResponse>> getCategories(@PathVariable UUID restaurantId) {
        return ResponseEntity.ok(menuService.getCategories(restaurantId));
    }

    @GetMapping("/categories/with-items")
    public ResponseEntity<List<MenuCategoryResponse>> getCategoriesWithItems(@PathVariable UUID restaurantId) {
        return ResponseEntity.ok(menuService.getCategoriesWithItems(restaurantId));
    }

    // Item endpoints
    @PostMapping("/items")
    public ResponseEntity<MenuItemResponse> createItem(
            @PathVariable UUID restaurantId,
            @Valid @RequestBody MenuItemRequest request,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(menuService.createItem(restaurantId, userId, request));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<MenuItemResponse> updateItem(
            @PathVariable UUID restaurantId,
            @PathVariable UUID itemId,
            @Valid @RequestBody MenuItemRequest request,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(menuService.updateItem(itemId, userId, request));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable UUID restaurantId,
            @PathVariable UUID itemId,
            @AuthenticationPrincipal UUID userId) {
        menuService.deleteItem(itemId, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/items/{itemId}/availability")
    public ResponseEntity<MenuItemResponse> toggleAvailability(
            @PathVariable UUID restaurantId,
            @PathVariable UUID itemId,
            @RequestParam boolean available,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(menuService.toggleAvailability(itemId, userId, available));
    }

    @GetMapping("/items")
    public ResponseEntity<List<MenuItemResponse>> getItems(
            @PathVariable UUID restaurantId,
            @RequestParam(required = false) UUID categoryId) {
        return ResponseEntity.ok(menuService.getItems(restaurantId, categoryId));
    }

    @GetMapping("/items/{itemId}")
    public ResponseEntity<MenuItemResponse> getItem(
            @PathVariable UUID restaurantId,
            @PathVariable UUID itemId) {
        return ResponseEntity.ok(menuService.getItem(itemId));
    }
}