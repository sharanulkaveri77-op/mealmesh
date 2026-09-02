package com.mealmesh.delivery.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "delivery_partners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPartner {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private com.mealmesh.user.entity.User user;

    @Column(name = "employee_id", length = 50, unique = true)
    private String employeeId;

    @Column(name = "vehicle_type", length = 50)
    private String vehicleType;

    @Column(name = "vehicle_number", length = 50)
    private String vehicleNumber;

    @Column(name = "license_number", length = 50)
    private String licenseNumber;

    @Column(name = "license_expiry")
    private java.time.LocalDate licenseExpiry;

    @Column(name = "is_online", nullable = false)
    private Boolean isOnline = false;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = false;

    @Column(name = "current_latitude", precision = 10, scale = 8)
    private BigDecimal currentLatitude;

    @Column(name = "current_longitude", precision = 11, scale = 8)
    private BigDecimal currentLongitude;

    @Column(name = "last_location_update")
    private Instant lastLocationUpdate;

    @Column(name = "current_area", length = 100)
    private String currentArea;

    @Column(name = "max_delivery_radius_km", precision = 5, scale = 2)
    private BigDecimal maxDeliveryRadiusKm = new BigDecimal("10.00");

    @Column(name = "max_concurrent_orders")
    private Integer maxConcurrentOrders = 3;

    @Column(name = "current_active_orders")
    private Integer currentActiveOrders = 0;

    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating = new BigDecimal("5.00");

    @Column(name = "total_deliveries")
    private Integer totalDeliveries = 0;

    @Column(name = "total_earnings", precision = 12, scale = 2)
    private BigDecimal totalEarnings = BigDecimal.ZERO;

    @Column(name = "average_delivery_time_minutes")
    private Integer averageDeliveryTimeMinutes = 30;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "verification_documents", columnDefinition = "jsonb")
    private String verificationDocuments = "{}";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "bank_account_details", columnDefinition = "jsonb")
    private String bankAccountDetails;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}