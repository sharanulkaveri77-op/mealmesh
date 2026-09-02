package com.mealmesh.review.dto;

import com.mealmesh.review.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private UUID id;
    private UUID orderId;
    private String orderNumber;
    private UUID customerId;
    private String customerName;
    private UUID restaurantId;
    private String restaurantName;
    private Integer rating;
    private Integer foodRating;
    private Integer deliveryRating;
    private Integer packagingRating;
    private String comment;
    private String images;
    private Boolean isVerifiedPurchase;
    private String restaurantResponse;
    private Instant respondedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public static ReviewResponse fromEntity(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .orderId(review.getOrder().getId())
                .orderNumber(review.getOrder().getOrderNumber())
                .customerId(review.getCustomer().getId())
                .customerName(review.getCustomer().getName())
                .restaurantId(review.getRestaurant().getId())
                .restaurantName(review.getRestaurant().getName())
                .rating(review.getRating())
                .foodRating(review.getFoodRating())
                .deliveryRating(review.getDeliveryRating())
                .packagingRating(review.getPackagingRating())
                .comment(review.getComment())
                .images(review.getImages())
                .isVerifiedPurchase(review.getIsVerifiedPurchase())
                .restaurantResponse(review.getRestaurantResponse())
                .respondedAt(review.getRespondedAt())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
