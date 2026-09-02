package com.mealmesh.delivery.service;

import com.mealmesh.common.exception.ResourceNotFoundException;
import com.mealmesh.delivery.entity.DeliveryPartner;
import com.mealmesh.delivery.entity.DeliveryAssignment;
import com.mealmesh.delivery.repository.DeliveryPartnerRepository;
import com.mealmesh.delivery.repository.DeliveryAssignmentRepository;
import com.mealmesh.order.entity.Order;
import com.mealmesh.order.entity.OrderStatus;
import com.mealmesh.order.repository.OrderRepository;
import com.mealmesh.restaurant.entity.Restaurant;
import com.mealmesh.restaurant.repository.RestaurantRepository;
import com.mealmesh.kafka.event.DeliveryPartnerAssignedEvent;
import com.mealmesh.kafka.producer.EventProducer;
import com.mealmesh.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryMatchingService {

    private final DeliveryPartnerRepository partnerRepository;
    private final DeliveryAssignmentRepository assignmentRepository;
    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final EventProducer eventProducer;
    private final OutboxService outboxService;

    @Transactional
    public void matchAndAssignDeliveryPartner(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.READY_FOR_PICKUP) {
            log.warn("Order {} not ready for pickup, status: {}", orderId, order.getStatus());
            return;
        }

        Restaurant restaurant = order.getRestaurant();
        if (restaurant.getLatitude() == null || restaurant.getLongitude() == null) {
            log.warn("Restaurant {} has no location coordinates", restaurant.getId());
            return;
        }

        // Find nearby available delivery partners
        List<DeliveryPartner> nearbyPartners = partnerRepository.findAvailablePartnersNearLocation(
                restaurant.getLatitude(), 
                restaurant.getLongitude(),
                BigDecimal.valueOf(10) // 10km radius
        );

        if (nearbyPartners.isEmpty()) {
            log.warn("No available delivery partners near restaurant {}", restaurant.getId());
            return;
        }

        // Score and rank partners
        Optional<DeliveryPartner> bestPartner = scoreAndSelectPartner(nearbyPartners, restaurant);

        if (bestPartner.isPresent()) {
            assignPartner(order, bestPartner.get());
        } else {
            log.warn("Could not select a delivery partner for order {}", orderId);
        }
    }

    @Transactional
    public void assignPartner(Order order, DeliveryPartner partner) {
        DeliveryAssignment assignment = DeliveryAssignment.builder()
                .order(order)
                .deliveryPartner(partner)
                .status("ASSIGNED")
                .assignedAt(Instant.now())
                .estimatedPickupTime(Instant.now().plusSeconds(300)) // 5 minutes
                .build();

        assignmentRepository.save(assignment);

        // Update partner status
        partner.setCurrentActiveOrders(partner.getCurrentActiveOrders() + 1);
        if (partner.getCurrentActiveOrders() >= partner.getMaxConcurrentOrders()) {
            partner.setIsAvailable(false);
        }

        // Update order status
        order.setStatus(com.mealmesh.order.entity.OrderStatus.DELIVERY_PARTNER_ASSIGNED);
        order.setUpdatedAt(Instant.now());

        log.info("Assigned delivery partner {} to order {}", partner.getId(), order.getId());

        // Publish event
        DeliveryPartnerAssignedEvent event = DeliveryPartnerAssignedEvent.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .deliveryPartnerId(partner.getId())
                .partnerName(partner.getUser().getName())
                .partnerPhone(partner.getUser().getPhone())
                .assignmentId(assignment.getId())
                .estimatedPickupDistanceKm(BigDecimal.valueOf(2.5)) // Placeholder
                .estimatedPickupTimeMinutes(10)
                .assignedAt(Instant.now())
                .build();

        eventProducer.sendDeliveryPartnerAssigned(event);

        // Also save to outbox for guaranteed delivery
        outboxService.saveEvent("Delivery", order.getId(), "DeliveryPartnerAssignedEvent",
                java.util.Map.of(
                        "orderId", order.getId(),
                        "orderNumber", order.getOrderNumber(),
                        "deliveryPartnerId", partner.getId(),
                        "assignmentId", assignment.getId(),
                        "assignedAt", Instant.now().toString()
                ));
    }

    private Optional<DeliveryPartner> scoreAndSelectPartner(List<DeliveryPartner> partners, Restaurant restaurant) {
        return partners.stream()
                .map(partner -> new ScoredPartner(
                        partner,
                        calculateScore(partner, restaurant)
                ))
                .max((p1, p2) -> Double.compare(p1.score, p2.score))
                .map(sp -> sp.partner);
    }

    private double calculateScore(DeliveryPartner partner, Restaurant restaurant) {
        double score = 0;

        // Distance factor (closer is better)
        double distance = calculateDistance(
                restaurant.getLatitude(), restaurant.getLongitude(),
                partner.getCurrentLatitude(), partner.getCurrentLongitude()
        );
        score += (10 - distance) * 10; // Max 100 for 0km

        // Rating factor
        score += partner.getRating().doubleValue() * 5;

        // Active orders factor (fewer is better)
        score += (partner.getMaxConcurrentOrders() - partner.getCurrentActiveOrders()) * 10;

        // Performance factor
        score += (100 - partner.getAverageDeliveryTimeMinutes()) * 0.5;

        return score;
    }

    private double calculateDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return 10.0;
        }
        // Haversine formula
        double lat1Rad = Math.toRadians(lat1.doubleValue());
        double lon1Rad = Math.toRadians(lon1.doubleValue());
        double lat2Rad = Math.toRadians(lat2.doubleValue());
        double lon2Rad = Math.toRadians(lon2.doubleValue());

        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371 * c; // Earth radius in km
    }

    private record ScoredPartner(DeliveryPartner partner, double score) {}
}