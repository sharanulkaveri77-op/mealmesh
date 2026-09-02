package com.mealmesh.restaurant.controller;

import com.mealmesh.restaurant.dto.RestaurantRequest;
import com.mealmesh.restaurant.dto.RestaurantResponse;
import com.mealmesh.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final com.mealmesh.restaurant.service.RestaurantSearchService searchService;

    @PostMapping("/filter-search")
    public ResponseEntity<List<RestaurantResponse>> filterSearch(
            @RequestBody com.mealmesh.restaurant.dto.RestaurantSearchRequest request) {
        return ResponseEntity.ok(searchService.search(request));
    }

    @PostMapping
    public ResponseEntity<RestaurantResponse> create(
            @Valid @RequestBody RestaurantRequest request,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(restaurantService.create(request, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody RestaurantRequest request,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(restaurantService.update(id, request, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(restaurantService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<RestaurantResponse>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(restaurantService.getAllActive(pageable));
    }

    @GetMapping("/list")
    public ResponseEntity<List<RestaurantResponse>> getAllList() {
        return ResponseEntity.ok(restaurantService.getAllActive());
    }

    @GetMapping("/search")
    public ResponseEntity<List<RestaurantResponse>> searchNearby(
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lon,
            @RequestParam(defaultValue = "5.0") BigDecimal radius) {
        return ResponseEntity.ok(restaurantService.search(lat, lon, radius));
    }

    @GetMapping("/my")
    public ResponseEntity<List<RestaurantResponse>> getMyRestaurants(
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(restaurantService.getByOwner(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId) {
        restaurantService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<RestaurantResponse> toggleActive(
            @PathVariable UUID id,
            @RequestParam boolean active,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(restaurantService.toggleActive(id, userId, active));
    }

    @PatchMapping("/{id}/accepting-orders")
    public ResponseEntity<RestaurantResponse> toggleAcceptingOrders(
            @PathVariable UUID id,
            @RequestParam boolean accepting,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(restaurantService.toggleAcceptingOrders(id, userId, accepting));
    }
}