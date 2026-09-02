package com.mealmesh.analytics.service;

import com.mealmesh.analytics.dto.AdminDashboardMetricsResponse;
import com.mealmesh.analytics.dto.RestaurantAnalyticsResponse;
import com.mealmesh.common.exception.ResourceNotFoundException;
import com.mealmesh.delivery.repository.DeliveryPartnerRepository;
import com.mealmesh.order.entity.Order;
import com.mealmesh.order.entity.OrderItem;
import com.mealmesh.order.entity.OrderStatus;
import com.mealmesh.order.repository.OrderItemRepository;
import com.mealmesh.order.repository.OrderRepository;
import com.mealmesh.restaurant.entity.Restaurant;
import com.mealmesh.restaurant.repository.RestaurantRepository;
import com.mealmesh.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional(readOnly = true)
    public AdminDashboardMetricsResponse getAdminDashboardMetrics() {
        List<Order> allOrders = orderRepository.findAll();

        BigDecimal gmv = allOrders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED && o.getStatus() != OrderStatus.RESTAURANT_REJECTED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalOrders = allOrders.size();
        long completed = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
        long cancelled = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED || o.getStatus() == OrderStatus.RESTAURANT_REJECTED).count();

        BigDecimal aov = totalOrders > 0
                ? gmv.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<String, Long> statusCounts = allOrders.stream()
                .collect(Collectors.groupingBy(o -> o.getStatus().name(), Collectors.counting()));

        long usersCount = userRepository.count();
        long activeRestaurantsCount = restaurantRepository.findByIsActiveTrueAndIsAcceptingOrdersTrue().size();
        long onlinePartnersCount = deliveryPartnerRepository.countByIsOnlineTrue();

        // Top restaurants
        Map<Restaurant, List<Order>> ordersByRestaurant = allOrders.stream()
                .collect(Collectors.groupingBy(Order::getRestaurant));

        List<AdminDashboardMetricsResponse.TopRestaurantMetric> topRestaurants = ordersByRestaurant.entrySet().stream()
                .map(entry -> {
                    Restaurant r = entry.getKey();
                    List<Order> orders = entry.getValue();
                    BigDecimal revenue = orders.stream()
                            .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                            .map(Order::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return AdminDashboardMetricsResponse.TopRestaurantMetric.builder()
                            .name(r.getName())
                            .orderCount(orders.size())
                            .totalRevenue(revenue)
                            .rating(r.getRating())
                            .build();
                })
                .sorted(Comparator.comparing(AdminDashboardMetricsResponse.TopRestaurantMetric::getTotalRevenue).reversed())
                .limit(5)
                .collect(Collectors.toList());

        return AdminDashboardMetricsResponse.builder()
                .totalGrossMerchandiseValue(gmv)
                .totalOrdersCount(totalOrders)
                .totalCompletedOrders(completed)
                .totalCancelledOrders(cancelled)
                .totalRegisteredUsers(usersCount)
                .totalActiveRestaurants(activeRestaurantsCount)
                .totalOnlineDeliveryPartners(onlinePartnersCount)
                .averageOrderValue(aov)
                .ordersByStatus(statusCounts)
                .topPerformingRestaurants(topRestaurants)
                .build();
    }

    @Transactional(readOnly = true)
    public RestaurantAnalyticsResponse getRestaurantAnalytics(UUID restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + restaurantId));

        List<Order> orders = orderRepository.findByRestaurantAndStatusIn(
                restaurant,
                List.of(OrderStatus.values())
        );

        BigDecimal revenue = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long total = orders.size();
        long completed = orders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
        long cancelled = orders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED || o.getStatus() == OrderStatus.RESTAURANT_REJECTED).count();

        BigDecimal aov = completed > 0
                ? revenue.divide(BigDecimal.valueOf(completed), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Top selling items
        Map<String, List<OrderItem>> itemsByName = orders.stream()
                .flatMap(o -> o.getOrderItems().stream())
                .collect(Collectors.groupingBy(OrderItem::getMenuItemName));

        List<RestaurantAnalyticsResponse.TopMenuItemMetric> topItems = itemsByName.entrySet().stream()
                .map(entry -> {
                    String name = entry.getKey();
                    List<OrderItem> items = entry.getValue();
                    long quantity = items.stream().mapToLong(OrderItem::getQuantity).sum();
                    BigDecimal itemRev = items.stream().map(OrderItem::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);

                    return RestaurantAnalyticsResponse.TopMenuItemMetric.builder()
                            .itemName(name)
                            .totalQuantitySold(quantity)
                            .totalRevenue(itemRev)
                            .build();
                })
                .sorted(Comparator.comparing(RestaurantAnalyticsResponse.TopMenuItemMetric::getTotalQuantitySold).reversed())
                .limit(5)
                .collect(Collectors.toList());

        return RestaurantAnalyticsResponse.builder()
                .restaurantId(restaurant.getId())
                .restaurantName(restaurant.getName())
                .totalRevenue(revenue)
                .totalOrders(total)
                .completedOrders(completed)
                .cancelledOrders(cancelled)
                .averageOrderValue(aov)
                .currentRating(restaurant.getRating())
                .totalReviews(restaurant.getTotalReviews() != null ? restaurant.getTotalReviews() : 0)
                .topSellingItems(topItems)
                .build();
    }
}
