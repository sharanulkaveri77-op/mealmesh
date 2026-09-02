package com.mealmesh.analytics.controller;

import com.mealmesh.analytics.dto.AdminDashboardMetricsResponse;
import com.mealmesh.analytics.dto.RestaurantAnalyticsResponse;
import com.mealmesh.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardMetricsResponse> getAdminDashboardMetrics() {
        AdminDashboardMetricsResponse metrics = analyticsService.getAdminDashboardMetrics();
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<RestaurantAnalyticsResponse> getRestaurantAnalytics(
            @PathVariable UUID restaurantId) {
        RestaurantAnalyticsResponse analytics = analyticsService.getRestaurantAnalytics(restaurantId);
        return ResponseEntity.ok(analytics);
    }
}
