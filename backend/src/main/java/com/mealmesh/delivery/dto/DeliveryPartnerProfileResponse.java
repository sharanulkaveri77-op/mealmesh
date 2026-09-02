package com.mealmesh.delivery.dto;

import com.mealmesh.delivery.entity.DeliveryPartner;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPartnerProfileResponse {

    private UUID id;
    private UUID userId;
    private String name;
    private String email;
    private String phone;
    private String employeeId;
    private String vehicleType;
    private String vehicleNumber;
    private Boolean isOnline;
    private Boolean isAvailable;
    private BigDecimal currentLatitude;
    private BigDecimal currentLongitude;
    private Instant lastLocationUpdate;
    private BigDecimal rating;
    private Integer totalDeliveries;
    private BigDecimal totalEarnings;
    private Integer currentActiveOrders;
    private Integer maxConcurrentOrders;
    private Boolean isVerified;

    public static DeliveryPartnerProfileResponse fromEntity(DeliveryPartner partner) {
        return DeliveryPartnerProfileResponse.builder()
                .id(partner.getId())
                .userId(partner.getUser().getId())
                .name(partner.getUser().getName())
                .email(partner.getUser().getEmail())
                .phone(partner.getUser().getPhone())
                .employeeId(partner.getEmployeeId())
                .vehicleType(partner.getVehicleType())
                .vehicleNumber(partner.getVehicleNumber())
                .isOnline(partner.getIsOnline())
                .isAvailable(partner.getIsAvailable())
                .currentLatitude(partner.getCurrentLatitude())
                .currentLongitude(partner.getCurrentLongitude())
                .lastLocationUpdate(partner.getLastLocationUpdate())
                .rating(partner.getRating())
                .totalDeliveries(partner.getTotalDeliveries())
                .totalEarnings(partner.getTotalEarnings())
                .currentActiveOrders(partner.getCurrentActiveOrders())
                .maxConcurrentOrders(partner.getMaxConcurrentOrders())
                .isVerified(partner.getIsVerified())
                .build();
    }
}
