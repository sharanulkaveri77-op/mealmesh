package com.mealmesh.delivery.repository;

import com.mealmesh.delivery.entity.DeliveryPartner;
import com.mealmesh.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, UUID> {

    Optional<DeliveryPartner> findByUser(com.mealmesh.user.entity.User user);

    List<DeliveryPartner> findByIsOnlineTrueAndIsAvailableTrue();

    @Query(value = """
        SELECT dp.* FROM delivery_partners dp
        WHERE dp.is_online = true
        AND dp.is_available = true
        AND dp.current_latitude IS NOT NULL
        AND dp.current_longitude IS NOT NULL
        AND (
            6371 * acos(
                cos(radians(:lat)) * cos(radians(dp.current_latitude)) *
                cos(radians(dp.current_longitude) - radians(:lon)) +
                sin(radians(:lat)) * sin(radians(dp.current_latitude))
            )
        ) <= :radius
        AND dp.current_active_orders < dp.max_concurrent_orders
        """, nativeQuery = true)
    List<DeliveryPartner> findAvailablePartnersNearLocation(
            BigDecimal lat, 
            BigDecimal lon, 
            BigDecimal radius
    );

    long countByIsOnlineTrue();
}