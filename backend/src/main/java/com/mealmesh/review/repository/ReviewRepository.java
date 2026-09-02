package com.mealmesh.review.repository;

import com.mealmesh.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Page<Review> findByRestaurantIdOrderByCreatedAtDesc(UUID restaurantId, Pageable pageable);

    Page<Review> findByCustomerIdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);

    Optional<Review> findByOrderId(UUID orderId);

    boolean existsByOrderId(UUID orderId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.restaurant.id = :restaurantId")
    Double calculateAverageRatingByRestaurantId(@Param("restaurantId") UUID restaurantId);

    @Query("SELECT AVG(r.foodRating) FROM Review r WHERE r.restaurant.id = :restaurantId AND r.foodRating IS NOT NULL")
    Double calculateAverageFoodRatingByRestaurantId(@Param("restaurantId") UUID restaurantId);

    @Query("SELECT AVG(r.deliveryRating) FROM Review r WHERE r.restaurant.id = :restaurantId AND r.deliveryRating IS NOT NULL")
    Double calculateAverageDeliveryRatingByRestaurantId(@Param("restaurantId") UUID restaurantId);

    @Query("SELECT AVG(r.packagingRating) FROM Review r WHERE r.restaurant.id = :restaurantId AND r.packagingRating IS NOT NULL")
    Double calculateAveragePackagingRatingByRestaurantId(@Param("restaurantId") UUID restaurantId);

    Long countByRestaurantId(UUID restaurantId);

    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.restaurant.id = :restaurantId GROUP BY r.rating")
    List<Object[]> countRatingsGroupedByStars(@Param("restaurantId") UUID restaurantId);
}
