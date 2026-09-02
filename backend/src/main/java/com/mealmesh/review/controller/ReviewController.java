package com.mealmesh.review.controller;

import com.mealmesh.review.dto.RatingSummaryResponse;
import com.mealmesh.review.dto.RestaurantResponseRequest;
import com.mealmesh.review.dto.ReviewCreateRequest;
import com.mealmesh.review.dto.ReviewResponse;
import com.mealmesh.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @AuthenticationPrincipal UUID customerId,
            @Valid @RequestBody ReviewCreateRequest request) {
        ReviewResponse response = reviewService.createReview(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{reviewId}/respond")
    public ResponseEntity<ReviewResponse> respondToReview(
            @AuthenticationPrincipal UUID ownerId,
            @PathVariable UUID reviewId,
            @Valid @RequestBody RestaurantResponseRequest request) {
        ReviewResponse response = reviewService.respondToReview(ownerId, reviewId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<Page<ReviewResponse>> getRestaurantReviews(
            @PathVariable UUID restaurantId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ReviewResponse> response = reviewService.getRestaurantReviews(restaurantId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/restaurant/{restaurantId}/summary")
    public ResponseEntity<RatingSummaryResponse> getRestaurantRatingSummary(
            @PathVariable UUID restaurantId) {
        RatingSummaryResponse summary = reviewService.getRestaurantRatingSummary(restaurantId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/customer")
    public ResponseEntity<Page<ReviewResponse>> getCustomerReviews(
            @AuthenticationPrincipal UUID customerId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ReviewResponse> response = reviewService.getCustomerReviews(customerId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ReviewResponse> getReviewByOrder(@PathVariable UUID orderId) {
        ReviewResponse response = reviewService.getReviewByOrderId(orderId);
        return ResponseEntity.ok(response);
    }
}
