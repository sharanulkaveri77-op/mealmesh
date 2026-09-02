package com.mealmesh.loyalty.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedeemPointsRequest {

    @Min(value = 50, message = "Minimum 50 points required to redeem")
    private int points;
}
