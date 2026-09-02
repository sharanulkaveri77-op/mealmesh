package com.mealmesh.restaurant.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddressRequest {
    @NotBlank
    private String streetAddress;

    @NotBlank
    private String city;

    @NotBlank
    private String state;

    @NotBlank
    private String postalCode;

    private String country = "India";

    private BigDecimal latitude;
    private BigDecimal longitude;
    private Boolean isDefault = false;
}