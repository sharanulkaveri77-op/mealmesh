package com.mealmesh.coupon.repository;

import com.mealmesh.coupon.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, UUID> {

    Optional<Coupon> findByCodeIgnoreCase(String code);

    Optional<Coupon> findByCodeIgnoreCaseAndIsActiveTrue(String code);

    @Query("SELECT c FROM Coupon c WHERE c.isActive = true " +
           "AND c.validFrom <= :now AND c.validUntil >= :now " +
           "AND (c.usageLimit IS NULL OR c.usageCount < c.usageLimit) " +
           "ORDER BY c.createdAt DESC")
    List<Coupon> findAvailableActiveCoupons(@Param("now") Instant now);

    Page<Coupon> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
