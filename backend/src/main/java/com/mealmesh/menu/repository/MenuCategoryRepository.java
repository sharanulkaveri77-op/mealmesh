package com.mealmesh.menu.repository;

import com.mealmesh.menu.entity.MenuCategory;
import com.mealmesh.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MenuCategoryRepository extends JpaRepository<MenuCategory, UUID> {

    List<MenuCategory> findByRestaurantAndIsActiveTrueOrderByDisplayOrderAsc(Restaurant restaurant);

    List<MenuCategory> findByRestaurant(Restaurant restaurant);

    Optional<MenuCategory> findByIdAndRestaurant(UUID id, Restaurant restaurant);
}