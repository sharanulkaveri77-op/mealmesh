package com.mealmesh.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuCategoryResponse {
    private UUID id;
    private String name;
    private String description;
    private Integer displayOrder;
    private String imageUrl;
    private Boolean isActive;
    private List<MenuItemResponse> items;

    public static MenuCategoryResponse from(com.mealmesh.menu.entity.MenuCategory category) {
        return MenuCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .displayOrder(category.getDisplayOrder())
                .imageUrl(category.getImageUrl())
                .isActive(category.getIsActive())
                .build();
    }
}