package com.mealmesh.review.entity;

import com.mealmesh.order.entity.Order;
import com.mealmesh.restaurant.entity.Restaurant;
import com.mealmesh.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "food_rating")
    private Integer foodRating;

    @Column(name = "delivery_rating")
    private Integer deliveryRating;

    @Column(name = "packaging_rating")
    private Integer packagingRating;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "images", length = 1000)
    @Builder.Default
    private String images = "[]";

    @Column(name = "is_verified_purchase", nullable = false)
    @Builder.Default
    private Boolean isVerifiedPurchase = true;

    @Column(name = "restaurant_response", columnDefinition = "TEXT")
    private String restaurantResponse;

    @Column(name = "responded_at")
    private Instant respondedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
