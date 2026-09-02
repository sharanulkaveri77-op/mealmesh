package com.mealmesh.delivery.repository;

import com.mealmesh.delivery.entity.DeliveryAssignment;
import com.mealmesh.delivery.entity.DeliveryPartner;
import com.mealmesh.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryAssignmentRepository extends JpaRepository<DeliveryAssignment, UUID> {

    Optional<DeliveryAssignment> findByOrderId(UUID orderId);

    Optional<DeliveryAssignment> findByOrder(Order order);

    Optional<DeliveryAssignment> findByDeliveryPartnerAndStatusIn(
            DeliveryPartner partner, 
            List<String> statuses
    );

    Page<DeliveryAssignment> findByDeliveryPartnerIdOrderByAssignedAtDesc(
            UUID deliveryPartnerId, Pageable pageable);

    Page<DeliveryAssignment> findByDeliveryPartnerIdAndStatusInOrderByAssignedAtDesc(
            UUID deliveryPartnerId, List<String> statuses, Pageable pageable);

    long countByDeliveryPartnerIdAndStatus(UUID deliveryPartnerId, String status);
}