package com.mealmesh.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardMetricsResponse {

    private BigDecimal totalGrossMerchandiseValue;
    private long totalOrdersCount;
    private long totalCompletedOrders;
    private long totalCancelledOrders;
    private long totalRegisteredUsers;
    private long totalActiveRestaurants;
    private long totalOnlineDeliveryPartners;
    private BigDecimal averageOrderValue;
    private Map<String, Long> ordersByStatus;
    private List<TopRestaurantMetric> topPerformingRestaurants;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopRestaurantMetric {
        private String name;
        private long orderCount;
        private BigDecimal totalRevenue;
        private BigDecimal rating;
    }
}
