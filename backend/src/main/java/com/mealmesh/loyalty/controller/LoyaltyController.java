package com.mealmesh.loyalty.controller;

import com.mealmesh.loyalty.dto.LoyaltyResponse;
import com.mealmesh.loyalty.dto.RedeemPointsRequest;
import com.mealmesh.loyalty.service.LoyaltyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/loyalty")
@RequiredArgsConstructor
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    @GetMapping("/my")
    public ResponseEntity<LoyaltyResponse> getMyAccount(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(loyaltyService.getMyLoyaltyAccount(userId));
    }

    @PostMapping("/redeem")
    public ResponseEntity<LoyaltyResponse> redeemPoints(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody RedeemPointsRequest request) {
        return ResponseEntity.ok(loyaltyService.redeemPoints(userId, request));
    }
}
