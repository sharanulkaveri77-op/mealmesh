package com.mealmesh.restaurant.service;

import com.mealmesh.common.exception.BadRequestException;
import com.mealmesh.common.exception.ResourceNotFoundException;
import com.mealmesh.menu.entity.MenuItem;
import com.mealmesh.menu.repository.MenuItemRepository;
import com.mealmesh.order.dto.OrderResponse;
import com.mealmesh.order.dto.OrderStatusUpdateRequest;
import com.mealmesh.order.entity.OrderStatus;
import com.mealmesh.order.service.OrderService;
import com.mealmesh.restaurant.entity.Restaurant;
import com.mealmesh.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantOperationsService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderService orderService;

    private Restaurant verifyOwnership(UUID restaurantId, UUID ownerId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + restaurantId));
        if (!restaurant.getOwner().getId().equals(ownerId)) {
            throw new BadRequestException("You do not have permission to manage this restaurant");
        }
        return restaurant;
    }

    @Transactional
    @CacheEvict(value = "restaurant-menus", key = "#restaurantId")
    public MenuItem toggleMenuItemStock(UUID restaurantId, UUID itemId, UUID ownerId, boolean available) {
        Restaurant restaurant = verifyOwnership(restaurantId, ownerId);
        MenuItem item = menuItemRepository.findByIdAndRestaurant(itemId, restaurant)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + itemId));

        item.setIsAvailable(available);
        return menuItemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getLiveOrders(UUID restaurantId, UUID ownerId) {
        verifyOwnership(restaurantId, ownerId);
        List<OrderStatus> activeStatuses = List.of(
                OrderStatus.PAYMENT_CONFIRMED,
                OrderStatus.RESTAURANT_PENDING,
                OrderStatus.RESTAURANT_ACCEPTED,
                OrderStatus.PREPARING,
                OrderStatus.READY_FOR_PICKUP,
                OrderStatus.DELIVERY_PARTNER_ASSIGNED,
                OrderStatus.PICKED_UP,
                OrderStatus.OUT_FOR_DELIVERY
        );

        return orderService.getRestaurantOrdersByStatus(restaurantId, activeStatuses);
    }

    @Transactional
    public OrderResponse acceptOrder(UUID restaurantId, UUID orderId, UUID ownerId) {
        verifyOwnership(restaurantId, ownerId);
        OrderStatusUpdateRequest req = new OrderStatusUpdateRequest(OrderStatus.RESTAURANT_ACCEPTED, null);
        return orderService.updateOrderStatus(orderId, req, ownerId);
    }

    @Transactional
    public OrderResponse markOrderPreparing(UUID restaurantId, UUID orderId, UUID ownerId) {
        verifyOwnership(restaurantId, ownerId);
        OrderStatusUpdateRequest req = new OrderStatusUpdateRequest(OrderStatus.PREPARING, null);
        return orderService.updateOrderStatus(orderId, req, ownerId);
    }

    @Transactional
    public OrderResponse markOrderReady(UUID restaurantId, UUID orderId, UUID ownerId) {
        verifyOwnership(restaurantId, ownerId);
        OrderStatusUpdateRequest req = new OrderStatusUpdateRequest(OrderStatus.READY_FOR_PICKUP, null);
        return orderService.updateOrderStatus(orderId, req, ownerId);
    }

    @Transactional
    public OrderResponse rejectOrder(UUID restaurantId, UUID orderId, UUID ownerId, String reason) {
        verifyOwnership(restaurantId, ownerId);
        OrderStatusUpdateRequest req = new OrderStatusUpdateRequest(OrderStatus.RESTAURANT_REJECTED, reason);
        return orderService.updateOrderStatus(orderId, req, ownerId);
    }
}
