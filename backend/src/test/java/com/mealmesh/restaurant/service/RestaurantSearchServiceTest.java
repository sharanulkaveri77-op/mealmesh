package com.mealmesh.restaurant.service;

import com.mealmesh.restaurant.dto.RestaurantResponse;
import com.mealmesh.restaurant.dto.RestaurantSearchRequest;
import com.mealmesh.restaurant.entity.Restaurant;
import com.mealmesh.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantSearchServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private RestaurantSearchService searchService;

    private Restaurant r1;
    private Restaurant r2;

    @BeforeEach
    void setUp() {
        r1 = Restaurant.builder()
                .id(UUID.randomUUID())
                .name("Spice Garden")
                .description("Authentic Indian Curries")
                .cuisineTypes("[\"North Indian\", \"Mughlai\"]")
                .rating(new BigDecimal("4.80"))
                .deliveryFee(new BigDecimal("30.00"))
                .latitude(new BigDecimal("19.0760"))
                .longitude(new BigDecimal("72.8777"))
                .preparationTimeMinutes(25)
                .isActive(true)
                .isAcceptingOrders(true)
                .build();

        r2 = Restaurant.builder()
                .id(UUID.randomUUID())
                .name("Pizza Napoli")
                .description("Woodfired Italian pizza and pasta")
                .cuisineTypes("[\"Italian\", \"Pizza\"]")
                .rating(new BigDecimal("4.20"))
                .deliveryFee(new BigDecimal("50.00"))
                .latitude(new BigDecimal("19.0800"))
                .longitude(new BigDecimal("72.8800"))
                .preparationTimeMinutes(35)
                .isActive(true)
                .isAcceptingOrders(true)
                .build();
    }

    @Test
    @DisplayName("search should filter by keyword query")
    void search_byQuery() {
        when(restaurantRepository.findByIsActiveTrueAndIsAcceptingOrdersTrue()).thenReturn(List.of(r1, r2));

        RestaurantSearchRequest request = RestaurantSearchRequest.builder()
                .query("pizza")
                .build();

        List<RestaurantResponse> results = searchService.search(request);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Pizza Napoli");
    }

    @Test
    @DisplayName("search should filter by minimum rating")
    void search_byMinRating() {
        when(restaurantRepository.findByIsActiveTrueAndIsAcceptingOrdersTrue()).thenReturn(List.of(r1, r2));

        RestaurantSearchRequest request = RestaurantSearchRequest.builder()
                .minRating(new BigDecimal("4.50"))
                .build();

        List<RestaurantResponse> results = searchService.search(request);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Spice Garden");
    }

    @Test
    @DisplayName("calculateDistanceKm should calculate accurate distance between two points")
    void calculateDistance_accuracy() {
        // Distance between Mumbai (19.0760, 72.8777) and Pune (18.5204, 73.8567) is ~120 km
        double distance = searchService.calculateDistanceKm(19.0760, 72.8777, 18.5204, 73.8567);

        assertThat(distance).isBetween(110.0, 130.0);
    }
}
