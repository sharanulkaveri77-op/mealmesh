package com.mealmesh.menu.repository;

import com.mealmesh.menu.entity.MenuItem;
import com.mealmesh.menu.entity.MenuCategory;
import com.mealmesh.restaurant.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {

    List<MenuItem> findByRestaurantAndIsAvailableTrue(Restaurant restaurant);

    List<MenuItem> findByRestaurant(Restaurant restaurant);

    Page<MenuItem> findByRestaurantAndIsAvailableTrue(Restaurant restaurant, Pageable pageable);

    List<MenuItem> findByCategoryAndIsAvailableTrueOrderByDisplayOrderAsc(MenuCategory category);

    List<MenuItem> findByCategory(MenuCategory category);

    List<MenuItem> findByRestaurantAndIsFeaturedTrueAndIsAvailableTrue(Restaurant restaurant);

    Optional<MenuItem> findByIdAndRestaurant(UUID id, Restaurant restaurant);

    @Query("SELECT m FROM MenuItem m WHERE m.restaurant = :restaurant AND m.isAvailable = true AND " +
           "(LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(m.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<MenuItem> searchByRestaurantAndName(Restaurant restaurant, String search);
}