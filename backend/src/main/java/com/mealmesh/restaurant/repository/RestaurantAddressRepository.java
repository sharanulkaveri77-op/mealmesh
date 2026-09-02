package com.mealmesh.restaurant.repository;

import com.mealmesh.restaurant.entity.Restaurant;
import com.mealmesh.restaurant.entity.RestaurantAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RestaurantAddressRepository extends JpaRepository<RestaurantAddress, UUID> {

    List<RestaurantAddress> findByRestaurant(Restaurant restaurant);

    Optional<RestaurantAddress> findByRestaurantAndIsDefaultTrue(Restaurant restaurant);
}