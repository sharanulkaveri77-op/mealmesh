package com.mealmesh.recommendation.service;

import com.mealmesh.menu.dto.MenuItemResponse;
import com.mealmesh.menu.entity.MenuItem;
import com.mealmesh.menu.repository.MenuItemRepository;
import com.mealmesh.order.entity.Order;
import com.mealmesh.order.entity.OrderItem;
import com.mealmesh.order.entity.OrderStatus;
import com.mealmesh.order.repository.OrderRepository;
import com.mealmesh.recommendation.dto.RecommendationResponse;
import com.mealmesh.restaurant.entity.Restaurant;
import com.mealmesh.restaurant.repository.RestaurantRepository;
import com.mealmesh.user.entity.User;
import com.mealmesh.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public RecommendationResponse getPersonalizedRecommendations(UUID userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return getTrendingRecommendations();
        }

        List<Order> userOrders = orderRepository.findByCustomerAndStatusIn(user, List.of(OrderStatus.DELIVERED));

        if (userOrders.isEmpty()) {
            return getTrendingRecommendations();
        }

        // Extract menu item IDs ordered before
        Set<UUID> orderedItemIds = userOrders.stream()
                .flatMap(o -> o.getOrderItems().stream())
                .map(item -> item.getMenuItem() != null ? item.getMenuItem().getId() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Find restaurants user loves
        Set<Restaurant> favoriteRestaurants = userOrders.stream()
                .map(Order::getRestaurant)
                .collect(Collectors.toSet());

        List<MenuItem> recommendations = new ArrayList<>();
        for (Restaurant r : favoriteRestaurants) {
            List<MenuItem> items = menuItemRepository.findByRestaurantAndIsAvailableTrue(r);
            recommendations.addAll(items);
        }

        List<MenuItemResponse> responseItems = recommendations.stream()
                .filter(item -> !orderedItemIds.contains(item.getId()))
                .limit(8)
                .map(this::mapToResponse)
                .toList();

        if (responseItems.isEmpty()) {
            return getTrendingRecommendations();
        }

        return RecommendationResponse.builder()
                .sectionTitle("Recommended For You")
                .recommendationReason("Based on your favorite restaurants and taste preferences")
                .recommendedItems(responseItems)
                .build();
    }

    @Transactional(readOnly = true)
    public RecommendationResponse getQuickReorders(UUID userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return getTrendingRecommendations();
        }

        List<Order> deliveredOrders = orderRepository.findByCustomerAndStatusIn(user, List.of(OrderStatus.DELIVERED));

        List<MenuItemResponse> reorderItems = deliveredOrders.stream()
                .flatMap(o -> o.getOrderItems().stream())
                .map(OrderItem::getMenuItem)
                .filter(Objects::nonNull)
                .filter(MenuItem::getIsAvailable)
                .distinct()
                .limit(6)
                .map(this::mapToResponse)
                .toList();

        return RecommendationResponse.builder()
                .sectionTitle("Reorder Your Favorites")
                .recommendationReason("Dishes you've loved and ordered previously")
                .recommendedItems(reorderItems)
                .build();
    }

    @Transactional(readOnly = true)
    public RecommendationResponse getTrendingRecommendations() {
        List<MenuItem> featuredItems = menuItemRepository.findAll().stream()
                .filter(MenuItem::getIsAvailable)
                .filter(item -> item.getIsFeatured() != null && item.getIsFeatured())
                .limit(8)
                .toList();

        if (featuredItems.isEmpty()) {
            featuredItems = menuItemRepository.findAll().stream()
                    .filter(MenuItem::getIsAvailable)
                    .limit(8)
                    .toList();
        }

        return RecommendationResponse.builder()
                .sectionTitle("Popular in Your City")
                .recommendationReason("Most loved and highly rated dishes this week")
                .recommendedItems(featuredItems.stream().map(this::mapToResponse).toList())
                .build();
    }

    private MenuItemResponse mapToResponse(MenuItem item) {
        return MenuItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .originalPrice(item.getOriginalPrice())
                .imageUrl(item.getImageUrl())
                .isVegetarian(item.getIsVegetarian())
                .isVegan(item.getIsVegan())
                .isGlutenFree(item.getIsGlutenFree())
                .spiceLevel(item.getSpiceLevel())
                .preparationTimeMinutes(item.getPreparationTimeMinutes())
                .isAvailable(item.getIsAvailable())
                .isFeatured(item.getIsFeatured())
                .displayOrder(item.getDisplayOrder())
                .tags(item.getTags())
                .build();
    }
}
