package com.mealmesh.review.service;

import com.mealmesh.common.exception.BadRequestException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private ReviewService reviewService;

    private UUID customerId;
    private UUID restaurantId;
    private UUID orderId;
    private User customer;
    private User owner;
    private Restaurant restaurant;
    private Order order;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        restaurantId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        customer = User.builder()
                .id(customerId)
                .name("Alice Customer")
                .email("alice@example.com")
                .build();

        owner = User.builder()
                .id(UUID.randomUUID())
                .name("Bob Owner")
                .build();

        restaurant = Restaurant.builder()
                .id(restaurantId)
                .name("Pizza Palace")
                .owner(owner)
                .rating(BigDecimal.ZERO)
                .totalReviews(0)
                .build();

        order = Order.builder()
                .id(orderId)
                .orderNumber("ORD-2026-001")
                .customer(customer)
                .restaurant(restaurant)
                .status(OrderStatus.DELIVERED)
                .build();
    }

    @Test
    @DisplayName("createReview should successfully save review, update restaurant rating, and outbox event")
    void createReview_success() {
        // Arrange
        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .orderId(orderId)
                .rating(5)
                .foodRating(5)
                .deliveryRating(4)
                .packagingRating(5)
                .comment("Delicious food!")
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(reviewRepository.existsByOrderId(orderId)).thenReturn(false);
        when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> {
            Review r = inv.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });
        when(reviewRepository.calculateAverageRatingByRestaurantId(restaurantId)).thenReturn(4.8);
        when(reviewRepository.countByRestaurantId(restaurantId)).thenReturn(10L);

        // Act
        ReviewResponse response = reviewService.createReview(customerId, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getComment()).isEqualTo("Delicious food!");

        // Verify outbox saved
        verify(outboxService).saveEvent(eq("Review"), any(UUID.class), eq("ReviewCreatedEvent"), any());

        // Verify restaurant rating updated
        ArgumentCaptor<Restaurant> captor = ArgumentCaptor.forClass(Restaurant.class);
        verify(restaurantRepository).save(captor.capture());
        Restaurant updated = captor.getValue();
        assertThat(updated.getRating()).isEqualTo(new BigDecimal("4.80"));
        assertThat(updated.getTotalReviews()).isEqualTo(10);
    }

    @Test
    @DisplayName("createReview should throw exception if order is not DELIVERED")
    void createReview_notDelivered() {
        // Arrange
        order.setStatus(OrderStatus.PAYMENT_CONFIRMED);
        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .orderId(orderId)
                .rating(5)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThatThrownBy(() -> reviewService.createReview(customerId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Reviews can only be submitted for DELIVERED orders");

        verifyNoInteractions(outboxService);
    }

    @Test
    @DisplayName("createReview should throw exception if order already has a review")
    void createReview_duplicateReview() {
        // Arrange
        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .orderId(orderId)
                .rating(4)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(reviewRepository.existsByOrderId(orderId)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> reviewService.createReview(customerId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already been reviewed");

        verifyNoInteractions(outboxService);
    }

    @Test
    @DisplayName("respondToReview should update review with owner response")
    void respondToReview_success() {
        // Arrange
        UUID reviewId = UUID.randomUUID();
        Review review = Review.builder()
                .id(reviewId)
                .order(order)
                .customer(customer)
                .restaurant(restaurant)
                .rating(5)
                .build();

        RestaurantResponseRequest req = RestaurantResponseRequest.builder()
                .response("Thank you for your lovely feedback!")
                .build();

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        ReviewResponse res = reviewService.respondToReview(owner.getId(), reviewId, req);

        // Assert
        assertThat(res.getRestaurantResponse()).isEqualTo("Thank you for your lovely feedback!");
        assertThat(res.getRespondedAt()).isNotNull();
    }

    @Test
    @DisplayName("getRestaurantRatingSummary should calculate averages and star distribution")
    void getRestaurantRatingSummary_success() {
        // Arrange
        when(reviewRepository.calculateAverageRatingByRestaurantId(restaurantId)).thenReturn(4.5);
        when(reviewRepository.calculateAverageFoodRatingByRestaurantId(restaurantId)).thenReturn(4.7);
        when(reviewRepository.calculateAverageDeliveryRatingByRestaurantId(restaurantId)).thenReturn(4.2);
        when(reviewRepository.calculateAveragePackagingRatingByRestaurantId(restaurantId)).thenReturn(4.6);
        when(reviewRepository.countByRestaurantId(restaurantId)).thenReturn(25L);
        when(reviewRepository.countRatingsGroupedByStars(restaurantId)).thenReturn(List.<Object[]>of(
                new Object[]{5, 18L},
                new Object[]{4, 5L},
                new Object[]{3, 2L}
        ));

        // Act
        RatingSummaryResponse summary = reviewService.getRestaurantRatingSummary(restaurantId);

        // Assert
        assertThat(summary.getAverageRating()).isEqualTo(new BigDecimal("4.50"));
        assertThat(summary.getTotalReviews()).isEqualTo(25L);
        assertThat(summary.getStarDistribution().get(5)).isEqualTo(18L);
        assertThat(summary.getStarDistribution().get(4)).isEqualTo(5L);
        assertThat(summary.getStarDistribution().get(1)).isEqualTo(0L);
    }
}
