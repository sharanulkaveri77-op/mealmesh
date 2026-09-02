package com.mealmesh.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantAnalyticsResponse {

    private UUID restaurantId;
    private String restaurantName;
    private BigDecimal totalRevenue;
    private long totalOrders;
    private long completedOrders;
    private long cancelledOrders;
    private BigDecimal averageOrderValue;
    private BigDecimal currentRating;
    private int totalReviews;
    private List<TopMenuItemMetric> topSellingItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopMenuItemMetric {
        private String itemName;
        private long totalQuantitySold;
        private BigDecimal totalRevenue;
    }
}
