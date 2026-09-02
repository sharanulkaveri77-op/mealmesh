package com.mealmesh.restaurant.service;

import com.mealmesh.common.exception.BadRequestException;
import com.mealmesh.common.exception.ResourceNotFoundException;
import com.mealmesh.restaurant.dto.RestaurantRequest;
import com.mealmesh.restaurant.dto.RestaurantResponse;
import com.mealmesh.restaurant.entity.Restaurant;
import com.mealmesh.restaurant.entity.RestaurantAddress;
import com.mealmesh.restaurant.repository.RestaurantRepository;
import com.mealmesh.restaurant.repository.RestaurantAddressRepository;
import com.mealmesh.user.entity.User;
import com.mealmesh.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantAddressRepository addressRepository;
    private final UserRepository userRepository;

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public RestaurantResponse create(RestaurantRequest request, UUID ownerId) {
        User owner = getUser(ownerId);

        if (restaurantRepository.existsByOwnerAndName(owner, request.getName())) {
            throw new BadRequestException("Restaurant with this name already exists");
        }

        Restaurant restaurant = Restaurant.builder()
                .owner(owner)
                .name(request.getName())
                .description(request.getDescription())
                .phone(request.getPhone())
                .email(request.getEmail())
                .imageUrl(request.getImageUrl())
                .cuisineTypes(serializeCuisineTypes(request.getCuisineTypes()))
                .preparationTimeMinutes(request.getPreparationTimeMinutes() != null ? request.getPreparationTimeMinutes() : 30)
                .minimumOrderAmount(request.getMinimumOrderAmount() != null ? request.getMinimumOrderAmount() : BigDecimal.ZERO)
                .deliveryFee(request.getDeliveryFee() != null ? request.getDeliveryFee() : BigDecimal.ZERO)
                .deliveryRadiusKm(request.getDeliveryRadiusKm() != null ? request.getDeliveryRadiusKm() : new BigDecimal("5.00"))
                .openingTime(request.getOpeningTime() != null ? java.time.LocalTime.parse(request.getOpeningTime()) : null)
                .closingTime(request.getClosingTime() != null ? java.time.LocalTime.parse(request.getClosingTime()) : null)
                .isActive(true)
                .isAcceptingOrders(true)
                .build();

        restaurant = restaurantRepository.save(restaurant);

        if (request.getAddress() != null) {
            RestaurantAddress address = RestaurantAddress.builder()
                    .restaurant(restaurant)
                    .streetAddress(request.getAddress().getStreetAddress())
                    .city(request.getAddress().getCity())
                    .state(request.getAddress().getState())
                    .postalCode(request.getAddress().getPostalCode())
                    .country(request.getAddress().getCountry())
                    .latitude(request.getAddress().getLatitude())
                    .longitude(request.getAddress().getLongitude())
                    .isDefault(request.getAddress().getIsDefault() != null ? request.getAddress().getIsDefault() : true)
                    .build();
            addressRepository.save(address);
            restaurant.setLatitude(address.getLatitude());
            restaurant.setLongitude(address.getLongitude());
        }

        log.info("Restaurant created: {} by owner: {}", restaurant.getName(), owner.getEmail());
        return RestaurantResponse.from(restaurant);
    }

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "restaurants", key = "#id")
    public RestaurantResponse update(UUID id, RestaurantRequest request, UUID ownerId) {
        User owner = getUser(ownerId);
        Restaurant restaurant = restaurantRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found or access denied"));

        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());
        restaurant.setPhone(request.getPhone());
        restaurant.setEmail(request.getEmail());
        restaurant.setImageUrl(request.getImageUrl());
        restaurant.setCuisineTypes(serializeCuisineTypes(request.getCuisineTypes()));
        restaurant.setPreparationTimeMinutes(request.getPreparationTimeMinutes() != null ? request.getPreparationTimeMinutes() : restaurant.getPreparationTimeMinutes());
        restaurant.setMinimumOrderAmount(request.getMinimumOrderAmount() != null ? request.getMinimumOrderAmount() : restaurant.getMinimumOrderAmount());
        restaurant.setDeliveryFee(request.getDeliveryFee() != null ? request.getDeliveryFee() : restaurant.getDeliveryFee());
        restaurant.setDeliveryRadiusKm(request.getDeliveryRadiusKm() != null ? request.getDeliveryRadiusKm() : restaurant.getDeliveryRadiusKm());
        if (request.getOpeningTime() != null) {
            restaurant.setOpeningTime(java.time.LocalTime.parse(request.getOpeningTime()));
        }
        if (request.getClosingTime() != null) {
            restaurant.setClosingTime(java.time.LocalTime.parse(request.getClosingTime()));
        }

        restaurant = restaurantRepository.save(restaurant);
        log.info("Restaurant updated: {}", restaurant.getName());
        return RestaurantResponse.from(restaurant);
    }

    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "restaurants", key = "#id")
    public RestaurantResponse getById(UUID id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        return RestaurantResponse.from(restaurant);
    }

    @Transactional(readOnly = true)
    public Page<RestaurantResponse> getAllActive(Pageable pageable) {
        return restaurantRepository.findByIsActiveTrueAndIsAcceptingOrdersTrue(pageable)
                .map(RestaurantResponse::from);
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> getAllActive() {
        return restaurantRepository.findByIsActiveTrueAndIsAcceptingOrdersTrue().stream()
                .map(RestaurantResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> search(BigDecimal lat, BigDecimal lon, BigDecimal radius) {
        List<Restaurant> restaurants = restaurantRepository.findNearbyRestaurants(lat, lon, radius);
        return restaurants.stream().map(RestaurantResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> getByOwner(UUID ownerId) {
        User owner = getUser(ownerId);
        return restaurantRepository.findByOwner(owner).stream()
                .map(RestaurantResponse::from)
                .toList();
    }

    @Transactional
    public void delete(UUID id, UUID ownerId) {
        User owner = getUser(ownerId);
        Restaurant restaurant = restaurantRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found or access denied"));
        restaurantRepository.delete(restaurant);
        log.info("Restaurant deleted: {}", id);
    }

    @Transactional
    public RestaurantResponse toggleActive(UUID id, UUID ownerId, boolean active) {
        User owner = getUser(ownerId);
        Restaurant restaurant = restaurantRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found or access denied"));
        restaurant.setIsActive(active);
        restaurant = restaurantRepository.save(restaurant);
        return RestaurantResponse.from(restaurant);
    }

    @Transactional
    public RestaurantResponse toggleAcceptingOrders(UUID id, UUID ownerId, boolean accepting) {
        User owner = getUser(ownerId);
        Restaurant restaurant = restaurantRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found or access denied"));
        restaurant.setIsAcceptingOrders(accepting);
        restaurant = restaurantRepository.save(restaurant);
        return RestaurantResponse.from(restaurant);
    }

    private String serializeCuisineTypes(List<String> cuisineTypes) {
        if (cuisineTypes == null || cuisineTypes.isEmpty()) {
            return "[]";
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(cuisineTypes);
        } catch (Exception e) {
            return "[]";
        }
    }
}