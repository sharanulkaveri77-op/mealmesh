package com.mealmesh.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantSearchRequest {

    private String query;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal radiusKm;
    private List<String> cuisines;
    private BigDecimal minRating;
    private BigDecimal maxDeliveryFee;
    private Boolean isPureVeg;
    private Boolean isOpenNow;
    private String sortBy; // 'RATING', 'DISTANCE', 'DELIVERY_TIME', 'MIN_ORDER'
}
