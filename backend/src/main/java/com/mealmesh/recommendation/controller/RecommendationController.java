package com.mealmesh.recommendation.controller;

import com.mealmesh.recommendation.dto.RecommendationResponse;
import com.mealmesh.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/personalized")
    public ResponseEntity<RecommendationResponse> getPersonalized(
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(recommendationService.getPersonalizedRecommendations(userId));
    }

    @GetMapping("/reorders")
    public ResponseEntity<RecommendationResponse> getReorders(
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(recommendationService.getQuickReorders(userId));
    }

    @GetMapping("/trending")
    public ResponseEntity<RecommendationResponse> getTrending() {
        return ResponseEntity.ok(recommendationService.getTrendingRecommendations());
    }
}
