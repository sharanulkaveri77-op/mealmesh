package com.mealmesh.order.repository;

import com.mealmesh.order.entity.Order;
import com.mealmesh.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Page<Order> findByCustomer(com.mealmesh.user.entity.User customer, Pageable pageable);

    Page<Order> findByRestaurant(com.mealmesh.restaurant.entity.Restaurant restaurant, Pageable pageable);

    List<Order> findByRestaurantAndStatusIn(com.mealmesh.restaurant.entity.Restaurant restaurant, List<com.mealmesh.order.entity.OrderStatus> statuses);

    List<Order> findByCustomerAndStatusIn(com.mealmesh.user.entity.User customer, List<com.mealmesh.order.entity.OrderStatus> statuses);

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByPaymentId(UUID paymentId);

    long countByRestaurantAndStatusIn(com.mealmesh.restaurant.entity.Restaurant restaurant, List<com.mealmesh.order.entity.OrderStatus> statuses);

    long countByCustomer(com.mealmesh.user.entity.User customer);

    List<Order> findByStatus(com.mealmesh.order.entity.OrderStatus status);
}