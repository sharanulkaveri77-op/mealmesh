package com.mealmesh.payment.repository;

import com.mealmesh.payment.entity.Refund;
import com.mealmesh.payment.entity.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefundRepository extends JpaRepository<Refund, UUID> {

    List<Refund> findByUserId(UUID userId);

    Optional<Refund> findByOrderId(UUID orderId);

    List<Refund> findByStatus(RefundStatus status);
}
