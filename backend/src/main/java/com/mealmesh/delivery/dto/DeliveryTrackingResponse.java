package com.mealmesh.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryTrackingResponse {

    private UUID orderId;
    private String orderNumber;
    private String orderStatus;
    private String deliveryStatus;
    private UUID deliveryPartnerId;
    private String partnerName;
    private String partnerPhone;
    private BigDecimal partnerRating;
    private String vehicleType;
    private String vehicleNumber;
    private BigDecimal currentLatitude;
    private BigDecimal currentLongitude;
    private Instant lastLocationUpdate;
    private Instant estimatedDeliveryTime;
    private List<BreadcrumbPoint> breadcrumbs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BreadcrumbPoint {
        private BigDecimal latitude;
        private BigDecimal longitude;
        private Instant timestamp;
    }
}
