package com.mealmesh.recommendation.dto;

import com.mealmesh.menu.dto.MenuItemResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {

    private String sectionTitle;
    private String recommendationReason;
    private List<MenuItemResponse> recommendedItems;
}
