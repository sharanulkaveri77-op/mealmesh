package com.mealmesh.payment.repository;

import com.mealmesh.payment.entity.Payment;
import com.mealmesh.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByOrder(com.mealmesh.order.entity.Order order);

    Optional<Payment> findByOrderOrderId(UUID orderId);

    Optional<Payment> findByPaymentIdempotencyKey(String idempotencyKey);

    List<Payment> findByStatus(PaymentStatus status);

    Optional<Payment> findByProviderTransactionId(String providerTransactionId);

    Optional<Payment> findByOrderId(UUID orderId);
}