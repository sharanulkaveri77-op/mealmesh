package com.mealmesh.order.repository;

import com.mealmesh.order.entity.Order;
import com.mealmesh.order.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, UUID> {

    List<OrderStatusHistory> findByOrderOrderByCreatedAtAsc(Order order);

    List<OrderStatusHistory> findByOrderAndNewStatus(Order order, com.mealmesh.order.entity.OrderStatus status);
}