package com.mealmesh.coupon.repository;

import com.mealmesh.coupon.entity.CouponUsage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, UUID> {

    long countByCouponIdAndUserId(UUID couponId, UUID userId);

    Page<CouponUsage> findByUserIdOrderByUsedAtDesc(UUID userId, Pageable pageable);

    boolean existsByCouponIdAndUserIdAndOrderId(UUID couponId, UUID userId, UUID orderId);
}
