package com.mealmesh.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreateRequest {

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotNull(message = "Overall rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private Integer rating;

    @Min(value = 1, message = "Food rating must be at least 1")
    @Max(value = 5, message = "Food rating cannot exceed 5")
    private Integer foodRating;

    @Min(value = 1, message = "Delivery rating must be at least 1")
    @Max(value = 5, message = "Delivery rating cannot exceed 5")
    private Integer deliveryRating;

    @Min(value = 1, message = "Packaging rating must be at least 1")
    @Max(value = 5, message = "Packaging rating cannot exceed 5")
    private Integer packagingRating;

    @Size(max = 2000, message = "Comment cannot exceed 2000 characters")
    private String comment;

    private String images;
}
