package com.mealmesh.restaurant.dto;

import com.mealmesh.restaurant.entity.Restaurant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponse {
    private UUID id;
    private String name;
    private String description;
    private String phone;
    private String email;
    private String imageUrl;
    private List<String> cuisineTypes;
    private Boolean isActive;
    private Boolean isAcceptingOrders;
    private Integer preparationTimeMinutes;
    private BigDecimal minimumOrderAmount;
    private BigDecimal deliveryFee;
    private BigDecimal deliveryRadiusKm;
    private BigDecimal rating;
    private Integer totalReviews;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String openingTime;
    private String closingTime;
    private AddressResponse address;

    public static RestaurantResponse from(Restaurant restaurant) {
        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .description(restaurant.getDescription())
                .phone(restaurant.getPhone())
                .email(restaurant.getEmail())
                .imageUrl(restaurant.getImageUrl())
                .cuisineTypes(parseCuisineTypes(restaurant.getCuisineTypes()))
                .isActive(restaurant.getIsActive())
                .isAcceptingOrders(restaurant.getIsAcceptingOrders())
                .preparationTimeMinutes(restaurant.getPreparationTimeMinutes())
                .minimumOrderAmount(restaurant.getMinimumOrderAmount())
                .deliveryFee(restaurant.getDeliveryFee())
                .deliveryRadiusKm(restaurant.getDeliveryRadiusKm())
                .rating(restaurant.getRating())
                .totalReviews(restaurant.getTotalReviews())
                .latitude(restaurant.getLatitude())
                .longitude(restaurant.getLongitude())
                .openingTime(restaurant.getOpeningTime() != null ? restaurant.getOpeningTime().toString() : null)
                .closingTime(restaurant.getClosingTime() != null ? restaurant.getClosingTime().toString() : null)
                .build();
    }

    private static List<String> parseCuisineTypes(String json) {
        if (json == null || json.trim().isEmpty() || json.equals("[]")) {
            return List.of();
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (Exception e) {
            return List.of();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressResponse {
        private UUID id;
        private String label;
        private String streetAddress;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private Boolean isDefault;
    }
}