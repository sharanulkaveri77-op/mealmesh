package com.mealmesh.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponseRequest {

    @NotBlank(message = "Response text cannot be empty")
    @Size(max = 2000, message = "Response cannot exceed 2000 characters")
    private String response;
}
