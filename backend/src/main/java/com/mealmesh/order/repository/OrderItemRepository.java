package com.mealmesh.order.repository;

import com.mealmesh.order.entity.Order;
import com.mealmesh.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    List<OrderItem> findByOrder(Order order);

    void deleteByOrder(Order order);
}