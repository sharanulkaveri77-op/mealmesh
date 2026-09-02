package com.mealmesh.restaurant.repository;

import com.mealmesh.restaurant.entity.Restaurant;
import com.mealmesh.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    List<Restaurant> findByOwner(User owner);

    Page<Restaurant> findByIsActiveTrueAndIsAcceptingOrdersTrue(Pageable pageable);

    List<Restaurant> findByIsActiveTrueAndIsAcceptingOrdersTrue();

    @Query(value = """
        SELECT r.* FROM restaurants r
        WHERE r.is_active = true
        AND r.is_accepting_orders = true
        AND (
            6371 * acos(
                cos(radians(:lat)) * cos(radians(r.latitude)) *
                cos(radians(r.longitude) - radians(:lon)) +
                sin(radians(:lat)) * sin(radians(r.latitude))
            )
        ) <= :radius
        ORDER BY (6371 * acos(
            cos(radians(:lat)) * cos(radians(r.latitude)) *
            cos(radians(r.longitude) - radians(:lon)) +
            sin(radians(:lat)) * sin(radians(r.latitude))
        )) ASC
        """, nativeQuery = true)
    List<Restaurant> findNearbyRestaurants(BigDecimal lat, BigDecimal lon, BigDecimal radius);

    Optional<Restaurant> findByIdAndOwner(UUID id, User owner);

    boolean existsByOwnerAndName(User owner, String name);
}