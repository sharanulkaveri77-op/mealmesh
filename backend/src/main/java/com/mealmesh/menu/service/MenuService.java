package com.mealmesh.menu.service;

import com.mealmesh.common.exception.BadRequestException;
import com.mealmesh.common.exception.ResourceNotFoundException;
import com.mealmesh.menu.dto.MenuCategoryRequest;
import com.mealmesh.menu.dto.MenuCategoryResponse;
import com.mealmesh.menu.dto.MenuItemRequest;
import com.mealmesh.menu.dto.MenuItemResponse;
import com.mealmesh.menu.entity.MenuCategory;
import com.mealmesh.menu.entity.MenuItem;
import com.mealmesh.menu.repository.MenuCategoryRepository;
import com.mealmesh.menu.repository.MenuItemRepository;
import com.mealmesh.restaurant.entity.Restaurant;
import com.mealmesh.restaurant.repository.RestaurantRepository;
import com.mealmesh.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {

    private final MenuCategoryRepository categoryRepository;
    private final MenuItemRepository itemRepository;
    private final RestaurantRepository restaurantRepository;

    // Category operations
    @Transactional
    public MenuCategoryResponse createCategory(UUID restaurantId, UUID ownerId, MenuCategoryRequest request) {
        Restaurant restaurant = getRestaurantOwnedBy(restaurantId, ownerId);

        MenuCategory category = MenuCategory.builder()
                .restaurant(restaurant)
                .name(request.getName())
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .imageUrl(request.getImageUrl())
                .isActive(request.getIsActive())
                .build();

        category = categoryRepository.save(category);
        return MenuCategoryResponse.from(category);
    }

    @Transactional
    public MenuCategoryResponse updateCategory(UUID restaurantId, UUID categoryId, UUID ownerId, MenuCategoryRequest request) {
        Restaurant restaurant = getRestaurantOwnedBy(restaurantId, ownerId);
        MenuCategory category = categoryRepository.findByIdAndRestaurant(categoryId, restaurant)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : category.getDisplayOrder());
        category.setImageUrl(request.getImageUrl());
        category.setIsActive(request.getIsActive());

        category = categoryRepository.save(category);
        return MenuCategoryResponse.from(category);
    }

    @Transactional
    public void deleteCategory(UUID restaurantId, UUID categoryId, UUID ownerId) {
        Restaurant restaurant = getRestaurantOwnedBy(restaurantId, ownerId);
        MenuCategory category = categoryRepository.findByIdAndRestaurant(categoryId, restaurant)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        categoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public List<MenuCategoryResponse> getCategories(UUID restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        return categoryRepository.findByRestaurantAndIsActiveTrueOrderByDisplayOrderAsc(restaurant).stream()
                .map(MenuCategoryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "restaurant-menus", key = "#restaurantId")
    public List<MenuCategoryResponse> getCategoriesWithItems(UUID restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        return categoryRepository.findByRestaurantAndIsActiveTrueOrderByDisplayOrderAsc(restaurant).stream()
                .map(category -> {
                    MenuCategoryResponse resp = MenuCategoryResponse.from(category);
                    List<MenuItemResponse> items = itemRepository.findByCategoryAndIsAvailableTrueOrderByDisplayOrderAsc(category).stream()
                            .map(this::mapToItemResponse)
                            .toList();
                    resp.setItems(items);
                    return resp;
                })
                .toList();
    }

    // Item operations
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "restaurant-menus", key = "#restaurantId")
    public MenuItemResponse createItem(UUID restaurantId, UUID ownerId, MenuItemRequest request) {
        Restaurant restaurant = getRestaurantOwnedBy(restaurantId, ownerId);
        MenuCategory category = categoryRepository.findByIdAndRestaurant(request.getCategoryId(), restaurant)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        MenuItem item = MenuItem.builder()
                .restaurant(restaurant)
                .category(category)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .originalPrice(request.getOriginalPrice())
                .imageUrl(request.getImageUrl())
                .isVegetarian(request.getIsVegetarian())
                .isVegan(request.getIsVegan())
                .isGlutenFree(request.getIsGlutenFree())
                .spiceLevel(request.getSpiceLevel() != null ? request.getSpiceLevel() : 0)
                .preparationTimeMinutes(request.getPreparationTimeMinutes() != null ? request.getPreparationTimeMinutes() : 15)
                .isAvailable(request.getIsAvailable())
                .isFeatured(request.getIsFeatured())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .tags(serializeTags(request.getTags()))
                .nutritionalInfo(request.getNutritionalInfo())
                .build();

        item = itemRepository.save(item);
        return mapToItemResponse(item);
    }

    @Transactional
    public MenuItemResponse updateItem(UUID itemId, UUID ownerId, MenuItemRequest request) {
        MenuItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        if (!item.getRestaurant().getOwner().getId().equals(ownerId)) {
            throw new ResourceNotFoundException("Menu item not found or access denied");
        }

        if (!item.getCategory().getId().equals(request.getCategoryId())) {
            MenuCategory newCategory = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            if (!newCategory.getRestaurant().getId().equals(item.getRestaurant().getId())) {
                throw new BadRequestException("Category does not belong to this restaurant");
            }
            item.setCategory(newCategory);
        }

        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setOriginalPrice(request.getOriginalPrice());
        item.setImageUrl(request.getImageUrl());
        item.setIsVegetarian(request.getIsVegetarian());
        item.setIsVegan(request.getIsVegan());
        item.setIsGlutenFree(request.getIsGlutenFree());
        item.setSpiceLevel(request.getSpiceLevel() != null ? request.getSpiceLevel() : item.getSpiceLevel());
        item.setPreparationTimeMinutes(request.getPreparationTimeMinutes() != null ? request.getPreparationTimeMinutes() : item.getPreparationTimeMinutes());
        item.setIsAvailable(request.getIsAvailable());
        item.setIsFeatured(request.getIsFeatured());
        item.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : item.getDisplayOrder());
        item.setTags(serializeTags(request.getTags()));
        item.setNutritionalInfo(request.getNutritionalInfo());

        item = itemRepository.save(item);
        return mapToItemResponse(item);
    }

    @Transactional
    public void deleteItem(UUID itemId, UUID ownerId) {
        MenuItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        if (!item.getRestaurant().getOwner().getId().equals(ownerId)) {
            throw new ResourceNotFoundException("Menu item not found or access denied");
        }
        itemRepository.delete(item);
    }

    @Transactional
    public MenuItemResponse toggleAvailability(UUID itemId, UUID ownerId, boolean available) {
        MenuItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        if (!item.getRestaurant().getOwner().getId().equals(ownerId)) {
            throw new ResourceNotFoundException("Menu item not found or access denied");
        }
        item.setIsAvailable(available);
        item = itemRepository.save(item);
        return mapToItemResponse(item);
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getItems(UUID restaurantId, UUID categoryId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        if (categoryId != null) {
            MenuCategory category = categoryRepository.findByIdAndRestaurant(categoryId, restaurant)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            return itemRepository.findByCategoryAndIsAvailableTrueOrderByDisplayOrderAsc(category).stream()
                    .map(this::mapToItemResponse)
                    .toList();
        }
        return itemRepository.findByRestaurantAndIsAvailableTrue(restaurant).stream()
                .map(this::mapToItemResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MenuItemResponse getItem(UUID itemId) {
        MenuItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
        return mapToItemResponse(item);
    }

    private Restaurant getRestaurantOwnedBy(UUID restaurantId, UUID ownerId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        if (!restaurant.getOwner().getId().equals(ownerId)) {
            throw new ResourceNotFoundException("Restaurant not found or access denied");
        }
        return restaurant;
    }

    private MenuItemResponse mapToItemResponse(MenuItem item) {
        return MenuItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .originalPrice(item.getOriginalPrice())
                .imageUrl(item.getImageUrl())
                .isVegetarian(item.getIsVegetarian())
                .isVegan(item.getIsVegan())
                .isGlutenFree(item.getIsGlutenFree())
                .spiceLevel(item.getSpiceLevel())
                .preparationTimeMinutes(item.getPreparationTimeMinutes())
                .isAvailable(item.getIsAvailable())
                .isFeatured(item.getIsFeatured())
                .displayOrder(item.getDisplayOrder())
                .tags(item.getTags())
                .build();
    }

    private String serializeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "[]";
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(tags);
        } catch (Exception e) {
            return "[]";
        }
    }
}