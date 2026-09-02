package com.mealmesh.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingSummaryResponse {

    private UUID restaurantId;
    private BigDecimal averageRating;
    private Long totalReviews;
    private BigDecimal averageFoodRating;
    private BigDecimal averageDeliveryRating;
    private BigDecimal averagePackagingRating;
    private Map<Integer, Long> starDistribution;
}
