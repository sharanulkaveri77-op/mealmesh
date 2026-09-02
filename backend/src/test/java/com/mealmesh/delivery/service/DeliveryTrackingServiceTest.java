package com.mealmesh.delivery.service;

import com.mealmesh.delivery.dto.DeliveryTrackingResponse;
import com.mealmesh.delivery.dto.LocationUpdateRequest;
import com.mealmesh.delivery.entity.DeliveryAssignment;
import com.mealmesh.delivery.entity.DeliveryLocation;
import com.mealmesh.delivery.entity.DeliveryPartner;
import com.mealmesh.delivery.repository.DeliveryAssignmentRepository;
import com.mealmesh.delivery.repository.DeliveryLocationRepository;
import com.mealmesh.delivery.repository.DeliveryPartnerRepository;
import com.mealmesh.order.entity.Order;
import com.mealmesh.order.entity.OrderStatus;
import com.mealmesh.order.repository.OrderRepository;
import com.mealmesh.outbox.service.OutboxService;
import com.mealmesh.user.entity.User;
import com.mealmesh.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryTrackingServiceTest {

    @Mock
    private DeliveryAssignmentRepository assignmentRepository;

    @Mock
    private DeliveryLocationRepository locationRepository;

    @Mock
    private DeliveryPartnerRepository partnerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private DeliveryTrackingService deliveryTrackingService;

    private UUID userId;
    private UUID orderId;
    private User user;
    private DeliveryPartner partner;
    private Order order;
    private DeliveryAssignment assignment;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        user = User.builder()
                .id(userId)
                .name("Rider Dave")
                .phone("9876543210")
                .build();

        partner = DeliveryPartner.builder()
                .id(UUID.randomUUID())
                .user(user)
                .rating(new BigDecimal("4.90"))
                .vehicleType("SCOOTER")
                .vehicleNumber("MH02CD5678")
                .build();

        order = Order.builder()
                .id(orderId)
                .orderNumber("ORD-LIVE-001")
                .status(OrderStatus.OUT_FOR_DELIVERY)
                .build();

        assignment = DeliveryAssignment.builder()
                .id(UUID.randomUUID())
                .order(order)
                .deliveryPartner(partner)
                .status("PICKED_UP")
                .estimatedDeliveryTime(Instant.now().plusSeconds(900))
                .build();
    }

    @Test
    @DisplayName("recordLocation should update partner coordinates and save location breadcrumb with outbox event")
    void recordLocation_success() {
        // Arrange
        LocationUpdateRequest request = LocationUpdateRequest.builder()
                .latitude(new BigDecimal("19.07600000"))
                .longitude(new BigDecimal("72.87770000"))
                .accuracyMeters(BigDecimal.valueOf(5))
                .speedKmph(BigDecimal.valueOf(35))
                .headingDegrees(BigDecimal.valueOf(180))
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(partnerRepository.findByUser(user)).thenReturn(Optional.of(partner));
        when(assignmentRepository.findByDeliveryPartnerAndStatusIn(eq(partner), anyList()))
                .thenReturn(Optional.of(assignment));

        // Act
        deliveryTrackingService.recordLocation(userId, request);

        // Assert
        assertThat(partner.getCurrentLatitude()).isEqualTo(request.getLatitude());
        assertThat(partner.getCurrentLongitude()).isEqualTo(request.getLongitude());
        verify(partnerRepository).save(partner);
        verify(locationRepository).save(any(DeliveryLocation.class));
        verify(outboxService).saveEvent(eq("Delivery"), eq(assignment.getId()), eq("DeliveryLocationUpdateEvent"), any());
    }

    @Test
    @DisplayName("trackOrder should return full live tracking response with breadcrumb trail")
    void trackOrder_success() {
        // Arrange
        DeliveryLocation loc1 = DeliveryLocation.builder()
                .latitude(new BigDecimal("19.0750"))
                .longitude(new BigDecimal("72.8760"))
                .recordedAt(Instant.now().minusSeconds(60))
                .build();

        DeliveryLocation loc2 = DeliveryLocation.builder()
                .latitude(new BigDecimal("19.0760"))
                .longitude(new BigDecimal("72.8777"))
                .recordedAt(Instant.now())
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(assignmentRepository.findByOrderId(orderId)).thenReturn(Optional.of(assignment));
        when(locationRepository.findByDeliveryAssignmentOrderByRecordedAtAsc(assignment))
                .thenReturn(List.of(loc1, loc2));

        // Act
        DeliveryTrackingResponse response = deliveryTrackingService.trackOrder(orderId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(orderId);
        assertThat(response.getPartnerName()).isEqualTo("Rider Dave");
        assertThat(response.getVehicleType()).isEqualTo("SCOOTER");
        assertThat(response.getBreadcrumbs()).hasSize(2);
        assertThat(response.getBreadcrumbs().get(0).getLatitude()).isEqualTo(new BigDecimal("19.0750"));
    }
}
