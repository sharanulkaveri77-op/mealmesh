package com.mealmesh.delivery.service;

import com.mealmesh.common.exception.ResourceNotFoundException;
import com.mealmesh.delivery.dto.DeliveryTrackingResponse;
import com.mealmesh.delivery.dto.LocationUpdateRequest;
import com.mealmesh.delivery.entity.DeliveryAssignment;
import com.mealmesh.delivery.entity.DeliveryLocation;
import com.mealmesh.delivery.entity.DeliveryPartner;
import com.mealmesh.delivery.repository.DeliveryAssignmentRepository;
import com.mealmesh.delivery.repository.DeliveryLocationRepository;
import com.mealmesh.delivery.repository.DeliveryPartnerRepository;
import com.mealmesh.order.entity.Order;
import com.mealmesh.order.repository.OrderRepository;
import com.mealmesh.outbox.service.OutboxService;
import com.mealmesh.user.entity.User;
import com.mealmesh.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryTrackingService {

    private final DeliveryAssignmentRepository assignmentRepository;
    private final DeliveryLocationRepository locationRepository;
    private final DeliveryPartnerRepository partnerRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OutboxService outboxService;

    @Transactional
    public void updateLocation(UUID orderId, BigDecimal latitude, BigDecimal longitude) {
        DeliveryAssignment assignment = assignmentRepository.findByOrderId(orderId).orElse(null);
        if (assignment == null) {
            log.warn("Cannot update location: no delivery assignment found for order: {}", orderId);
            return;
        }

        DeliveryLocation location = DeliveryLocation.builder()
                .deliveryAssignment(assignment)
                .latitude(latitude)
                .longitude(longitude)
                .recordedAt(Instant.now())
                .build();

        locationRepository.save(location);

        DeliveryPartner partner = assignment.getDeliveryPartner();
        if (partner != null) {
            partner.setCurrentLatitude(latitude);
            partner.setCurrentLongitude(longitude);
            partner.setLastLocationUpdate(Instant.now());
            partnerRepository.save(partner);
        }

        log.debug("Updated location for order {}: {}, {}", orderId, latitude, longitude);
    }

    @Transactional
    public void recordLocation(UUID userId, LocationUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        DeliveryPartner partner = partnerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery partner profile not found for user: " + userId));

        // Update partner's live coordinates
        partner.setCurrentLatitude(request.getLatitude());
        partner.setCurrentLongitude(request.getLongitude());
        partner.setLastLocationUpdate(Instant.now());
        partnerRepository.save(partner);

        // Find current active assignment if any
        Optional<DeliveryAssignment> activeAssignment = assignmentRepository.findByDeliveryPartnerAndStatusIn(
                partner, List.of("ASSIGNED", "ACCEPTED", "PICKED_UP")
        );

        if (activeAssignment.isPresent()) {
            DeliveryAssignment assignment = activeAssignment.get();
            DeliveryLocation location = DeliveryLocation.builder()
                    .deliveryAssignment(assignment)
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .accuracyMeters(request.getAccuracyMeters())
                    .speedKmph(request.getSpeedKmph())
                    .headingDegrees(request.getHeadingDegrees())
                    .recordedAt(Instant.now())
                    .build();

            locationRepository.save(location);

            // Publish location update event to outbox
            outboxService.saveEvent("Delivery", assignment.getId(), "DeliveryLocationUpdateEvent",
                    Map.of(
                            "assignmentId", assignment.getId(),
                            "orderId", assignment.getOrder().getId(),
                            "latitude", request.getLatitude(),
                            "longitude", request.getLongitude(),
                            "speed", request.getSpeedKmph() != null ? request.getSpeedKmph() : 0,
                            "heading", request.getHeadingDegrees() != null ? request.getHeadingDegrees() : 0,
                            "recordedAt", Instant.now().toString()
                    ));
        }

        log.debug("Live location recorded for partner {}: {}, {}", partner.getId(), request.getLatitude(), request.getLongitude());
    }

    @Transactional(readOnly = true)
    public DeliveryTrackingResponse trackOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        DeliveryAssignment assignment = assignmentRepository.findByOrderId(orderId).orElse(null);

        if (assignment == null) {
            return DeliveryTrackingResponse.builder()
                    .orderId(order.getId())
                    .orderNumber(order.getOrderNumber())
                    .orderStatus(order.getStatus().name())
                    .deliveryStatus("UNASSIGNED")
                    .breadcrumbs(List.of())
                    .build();
        }

        DeliveryPartner partner = assignment.getDeliveryPartner();
        List<DeliveryLocation> locationList = locationRepository.findByDeliveryAssignmentOrderByRecordedAtAsc(assignment);

        List<DeliveryTrackingResponse.BreadcrumbPoint> breadcrumbs = locationList.stream()
                .map(loc -> DeliveryTrackingResponse.BreadcrumbPoint.builder()
                        .latitude(loc.getLatitude())
                        .longitude(loc.getLongitude())
                        .timestamp(loc.getRecordedAt())
                        .build())
                .collect(Collectors.toList());

        return DeliveryTrackingResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderStatus(order.getStatus().name())
                .deliveryStatus(assignment.getStatus())
                .deliveryPartnerId(partner != null ? partner.getId() : null)
                .partnerName(partner != null && partner.getUser() != null ? partner.getUser().getName() : null)
                .partnerPhone(partner != null && partner.getUser() != null ? partner.getUser().getPhone() : null)
                .partnerRating(partner != null ? partner.getRating() : null)
                .vehicleType(partner != null ? partner.getVehicleType() : null)
                .vehicleNumber(partner != null ? partner.getVehicleNumber() : null)
                .currentLatitude(partner != null ? partner.getCurrentLatitude() : null)
                .currentLongitude(partner != null ? partner.getCurrentLongitude() : null)
                .lastLocationUpdate(partner != null ? partner.getLastLocationUpdate() : null)
                .estimatedDeliveryTime(assignment.getEstimatedDeliveryTime())
                .breadcrumbs(breadcrumbs)
                .build();
    }
}