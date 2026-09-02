package com.mealmesh.review.service;

import com.mealmesh.common.exception.BadRequestException;
import com.mealmesh.common.exception.ResourceNotFoundException;
import com.mealmesh.order.entity.Order;
import com.mealmesh.order.entity.OrderStatus;
import com.mealmesh.order.repository.OrderRepository;
import com.mealmesh.outbox.service.OutboxService;
import com.mealmesh.restaurant.entity.Restaurant;
import com.mealmesh.restaurant.repository.RestaurantRepository;
import com.mealmesh.review.dto.RatingSummaryResponse;
import com.mealmesh.review.dto.RestaurantResponseRequest;
import com.mealmesh.review.dto.ReviewCreateRequest;
import com.mealmesh.review.dto.ReviewResponse;
import com.mealmesh.review.entity.Review;
import com.mealmesh.review.repository.ReviewRepository;
import com.mealmesh.user.entity.User;
import com.mealmesh.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final OutboxService outboxService;

    @Transactional
    public ReviewResponse createReview(UUID customerId, ReviewCreateRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new BadRequestException("You can only review orders placed by your account");
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BadRequestException("Reviews can only be submitted for DELIVERED orders. Current status: " + order.getStatus());
        }

        if (reviewRepository.existsByOrderId(order.getId())) {
            throw new BadRequestException("This order has already been reviewed");
        }

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Restaurant restaurant = order.getRestaurant();

        Review review = Review.builder()
                .order(order)
                .customer(customer)
                .restaurant(restaurant)
                .rating(request.getRating())
                .foodRating(request.getFoodRating())
                .deliveryRating(request.getDeliveryRating())
                .packagingRating(request.getPackagingRating())
                .comment(request.getComment())
                .images(request.getImages() != null ? request.getImages() : "[]")
                .isVerifiedPurchase(true)
                .build();

        review = reviewRepository.save(review);
        log.info("Review created: id={}, orderId={}, restaurantId={}, rating={}",
                review.getId(), order.getId(), restaurant.getId(), request.getRating());

        // Recalculate restaurant aggregate ratings
        updateRestaurantRatingStats(restaurant);

        // Save event to outbox for downstream notifications/analytics
        outboxService.saveEvent("Review", review.getId(), "ReviewCreatedEvent",
                Map.of(
                        "reviewId", review.getId(),
                        "orderId", order.getId(),
                        "restaurantId", restaurant.getId(),
                        "customerId", customer.getId(),
                        "rating", review.getRating(),
                        "createdAt", Instant.now().toString()
                ));

        return ReviewResponse.fromEntity(review);
    }

    @Transactional
    public ReviewResponse respondToReview(UUID ownerId, UUID reviewId, RestaurantResponseRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        Restaurant restaurant = review.getRestaurant();
        if (restaurant.getOwner() == null || !restaurant.getOwner().getId().equals(ownerId)) {
            throw new BadRequestException("Only the owner of this restaurant can respond to its reviews");
        }

        review.setRestaurantResponse(request.getResponse());
        review.setRespondedAt(Instant.now());
        review = reviewRepository.save(review);

        log.info("Restaurant owner responded to review: id={}, restaurantId={}", reviewId, restaurant.getId());
        return ReviewResponse.fromEntity(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getRestaurantReviews(UUID restaurantId, Pageable pageable) {
        return reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId, pageable)
                .map(ReviewResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getCustomerReviews(UUID customerId, Pageable pageable) {
        return reviewRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable)
                .map(ReviewResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public ReviewResponse getReviewByOrderId(UUID orderId) {
        Review review = reviewRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found for order: " + orderId));
        return ReviewResponse.fromEntity(review);
    }

    @Transactional(readOnly = true)
    public RatingSummaryResponse getRestaurantRatingSummary(UUID restaurantId) {
        Double avgRating = reviewRepository.calculateAverageRatingByRestaurantId(restaurantId);
        Double avgFood = reviewRepository.calculateAverageFoodRatingByRestaurantId(restaurantId);
        Double avgDelivery = reviewRepository.calculateAverageDeliveryRatingByRestaurantId(restaurantId);
        Double avgPackaging = reviewRepository.calculateAveragePackagingRatingByRestaurantId(restaurantId);
        Long totalReviews = reviewRepository.countByRestaurantId(restaurantId);

        Map<Integer, Long> starDistribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            starDistribution.put(i, 0L);
        }

        List<Object[]> starCounts = reviewRepository.countRatingsGroupedByStars(restaurantId);
        for (Object[] row : starCounts) {
            Integer stars = (Integer) row[0];
            Long count = (Long) row[1];
            if (stars != null) {
                starDistribution.put(stars, count);
            }
        }

        return RatingSummaryResponse.builder()
                .restaurantId(restaurantId)
                .averageRating(avgRating != null ? BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .totalReviews(totalReviews != null ? totalReviews : 0L)
                .averageFoodRating(avgFood != null ? BigDecimal.valueOf(avgFood).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .averageDeliveryRating(avgDelivery != null ? BigDecimal.valueOf(avgDelivery).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .averagePackagingRating(avgPackaging != null ? BigDecimal.valueOf(avgPackaging).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .starDistribution(starDistribution)
                .build();
    }

    private void updateRestaurantRatingStats(Restaurant restaurant) {
        Double avg = reviewRepository.calculateAverageRatingByRestaurantId(restaurant.getId());
        Long total = reviewRepository.countByRestaurantId(restaurant.getId());

        restaurant.setRating(avg != null ? BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        restaurant.setTotalReviews(total != null ? total.intValue() : 0);
        restaurantRepository.save(restaurant);
    }
}
