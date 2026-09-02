package com.mealmesh.restaurant.service;

import com.mealmesh.restaurant.dto.RestaurantResponse;
import com.mealmesh.restaurant.dto.RestaurantSearchRequest;
import com.mealmesh.restaurant.entity.Restaurant;
import com.mealmesh.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantSearchService {

    private final RestaurantRepository restaurantRepository;

    private static final double EARTH_RADIUS_KM = 6371.0;

    @Transactional(readOnly = true)
    public List<RestaurantResponse> search(RestaurantSearchRequest request) {
        List<Restaurant> activeRestaurants = restaurantRepository.findByIsActiveTrueAndIsAcceptingOrdersTrue();

        return activeRestaurants.stream()
                .filter(r -> matchesQuery(r, request.getQuery()))
                .filter(r -> matchesRating(r, request.getMinRating()))
                .filter(r -> matchesDeliveryFee(r, request.getMaxDeliveryFee()))
                .filter(r -> matchesOpenStatus(r, request.getIsOpenNow()))
                .filter(r -> matchesCuisines(r, request.getCuisines()))
                .filter(r -> matchesDistance(r, request.getLatitude(), request.getLongitude(), request.getRadiusKm()))
                .sorted(getComparator(request.getSortBy(), request.getLatitude(), request.getLongitude()))
                .map(RestaurantResponse::from)
                .collect(Collectors.toList());
    }

    private boolean matchesQuery(Restaurant r, String query) {
        if (query == null || query.isBlank()) return true;
        String q = query.toLowerCase().trim();
        return (r.getName() != null && r.getName().toLowerCase().contains(q)) ||
               (r.getDescription() != null && r.getDescription().toLowerCase().contains(q)) ||
               (r.getCuisineTypes() != null && r.getCuisineTypes().toLowerCase().contains(q));
    }

    private boolean matchesRating(Restaurant r, BigDecimal minRating) {
        if (minRating == null) return true;
        return r.getRating() != null && r.getRating().compareTo(minRating) >= 0;
    }

    private boolean matchesDeliveryFee(Restaurant r, BigDecimal maxFee) {
        if (maxFee == null) return true;
        return r.getDeliveryFee() == null || r.getDeliveryFee().compareTo(maxFee) <= 0;
    }

    private boolean matchesOpenStatus(Restaurant r, Boolean isOpenNow) {
        if (isOpenNow == null || !isOpenNow) return true;
        if (r.getOpeningTime() == null || r.getClosingTime() == null) return true;
        LocalTime now = LocalTime.now();
        return now.isAfter(r.getOpeningTime()) && now.isBefore(r.getClosingTime());
    }

    private boolean matchesCuisines(Restaurant r, List<String> targetCuisines) {
        if (targetCuisines == null || targetCuisines.isEmpty()) return true;
        if (r.getCuisineTypes() == null) return false;
        String cuisineStr = r.getCuisineTypes().toLowerCase();
        return targetCuisines.stream().anyMatch(c -> cuisineStr.contains(c.toLowerCase()));
    }

    private boolean matchesDistance(Restaurant r, BigDecimal lat, BigDecimal lng, BigDecimal radiusKm) {
        if (lat == null || lng == null || radiusKm == null) return true;
        if (r.getLatitude() == null || r.getLongitude() == null) return true;

        double distance = calculateDistanceKm(
                lat.doubleValue(), lng.doubleValue(),
                r.getLatitude().doubleValue(), r.getLongitude().doubleValue()
        );
        return distance <= radiusKm.doubleValue();
    }

    private Comparator<Restaurant> getComparator(String sortBy, BigDecimal lat, BigDecimal lng) {
        if ("RATING".equalsIgnoreCase(sortBy)) {
            return Comparator.comparing(Restaurant::getRating, Comparator.nullsLast(Comparator.reverseOrder()));
        } else if ("DELIVERY_TIME".equalsIgnoreCase(sortBy)) {
            return Comparator.comparing(Restaurant::getPreparationTimeMinutes, Comparator.nullsLast(Comparator.naturalOrder()));
        } else if ("MIN_ORDER".equalsIgnoreCase(sortBy)) {
            return Comparator.comparing(Restaurant::getMinimumOrderAmount, Comparator.nullsLast(Comparator.naturalOrder()));
        } else if ("DISTANCE".equalsIgnoreCase(sortBy) && lat != null && lng != null) {
            return Comparator.comparingDouble(r -> {
                if (r.getLatitude() == null || r.getLongitude() == null) return Double.MAX_VALUE;
                return calculateDistanceKm(lat.doubleValue(), lng.doubleValue(),
                        r.getLatitude().doubleValue(), r.getLongitude().doubleValue());
            });
        }
        // Default sort: highest rating first
        return Comparator.comparing(Restaurant::getRating, Comparator.nullsLast(Comparator.reverseOrder()));
    }

    public double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
