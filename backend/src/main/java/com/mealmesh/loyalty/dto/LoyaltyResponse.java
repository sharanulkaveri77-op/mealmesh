package com.mealmesh.loyalty.dto;

import com.mealmesh.loyalty.entity.LoyaltyAccount;
import com.mealmesh.loyalty.entity.LoyaltyTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyResponse {

    private UUID accountId;
    private UUID userId;
    private Integer pointsBalance;
    private Integer lifetimePoints;
    private LoyaltyTier tier;
    private double tierMultiplier;
    private BigDecimal pointsValueRupees;
    private int pointsToNextTier;

    public static LoyaltyResponse from(LoyaltyAccount account) {
        BigDecimal rupeeValue = BigDecimal.valueOf(account.getPointsBalance())
                .divide(BigDecimal.valueOf(10), 2, RoundingMode.HALF_UP);

        int toNextTier = 0;
        if (account.getLifetimePoints() < 500) {
            toNextTier = 500 - account.getLifetimePoints();
        } else if (account.getLifetimePoints() < 2000) {
            toNextTier = 2000 - account.getLifetimePoints();
        } else if (account.getLifetimePoints() < 5000) {
            toNextTier = 5000 - account.getLifetimePoints();
        }

        return LoyaltyResponse.builder()
                .accountId(account.getId())
                .userId(account.getUser().getId())
                .pointsBalance(account.getPointsBalance())
                .lifetimePoints(account.getLifetimePoints())
                .tier(account.getTier())
                .tierMultiplier(account.getTier().getMultiplier())
                .pointsValueRupees(rupeeValue)
                .pointsToNextTier(toNextTier)
                .build();
    }
}
