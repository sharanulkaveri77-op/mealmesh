package com.mealmesh.menu.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class MenuCategoryRequest {
    @NotBlank(message = "Category name is required")
    @Size(max = 255)
    private String name;

    @Size(max = 1000)
    private String description;

    private Integer displayOrder;

    private String imageUrl;

    private Boolean isActive = true;
}