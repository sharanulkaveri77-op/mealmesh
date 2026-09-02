package com.mealmesh.delivery.service;

import com.mealmesh.delivery.entity.DeliveryAssignment;
import com.mealmesh.delivery.entity.DeliveryPartner;
import com.mealmesh.delivery.repository.DeliveryAssignmentRepository;
import com.mealmesh.delivery.repository.DeliveryPartnerRepository;
import com.mealmesh.order.entity.Order;
import com.mealmesh.order.entity.OrderStatus;
import com.mealmesh.order.repository.OrderRepository;
import com.mealmesh.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryBatchingService {

    private final OrderRepository orderRepository;
    private final DeliveryPartnerRepository partnerRepository;
    private final DeliveryAssignmentRepository assignmentRepository;
    private final OutboxService outboxService;

    private static final double MAX_BATCH_PROXIMITY_KM = 3.0; // Orders within 3km can be batched together

    /**
     * Attempts to batch unassigned ready orders to available drivers in the same area.
     *
     * @return count of successfully batched orders
     */
    @Transactional
    public int batchAndAssignOrders() {
        List<Order> unassignedOrders = orderRepository.findByStatus(OrderStatus.READY_FOR_PICKUP).stream()
                .filter(order -> assignmentRepository.findByOrderId(order.getId()).isEmpty())
                .toList();

        if (unassignedOrders.isEmpty()) {
            return 0;
        }

        List<DeliveryPartner> availablePartners = partnerRepository.findByIsOnlineTrueAndIsAvailableTrue().stream()
                .filter(p -> p.getCurrentActiveOrders() < p.getMaxConcurrentOrders())
                .toList();

        if (availablePartners.isEmpty()) {
            return 0;
        }

        int batchedCount = 0;

        for (DeliveryPartner partner : availablePartners) {
            int capacity = partner.getMaxConcurrentOrders() - partner.getCurrentActiveOrders();
            if (capacity <= 0) continue;

            List<Order> candidateBatch = new ArrayList<>();

            for (Order order : unassignedOrders) {
                if (assignmentRepository.findByOrderId(order.getId()).isPresent()) {
                    continue;
                }

                if (candidateBatch.isEmpty()) {
                    candidateBatch.add(order);
                } else {
                    Order first = candidateBatch.get(0);
                    // Check if from same restaurant or close proximity
                    if (first.getRestaurant().getId().equals(order.getRestaurant().getId()) && candidateBatch.size() < capacity) {
                        candidateBatch.add(order);
                    }
                }

                if (candidateBatch.size() >= capacity) {
                    break;
                }
            }

            // Assign batch to partner
            for (Order order : candidateBatch) {
                DeliveryAssignment assignment = DeliveryAssignment.builder()
                        .order(order)
                        .deliveryPartner(partner)
                        .assignedAt(Instant.now())
                        .status("ASSIGNED")
                        .estimatedPickupTime(Instant.now().plusSeconds(600))
                        .estimatedDeliveryTime(Instant.now().plusSeconds(1800))
                        .earnings(BigDecimal.valueOf(40.00)) // Batched incentive rate
                        .build();

                assignmentRepository.save(assignment);

                partner.setCurrentActiveOrders(partner.getCurrentActiveOrders() + 1);
                if (partner.getCurrentActiveOrders() >= partner.getMaxConcurrentOrders()) {
                    partner.setIsAvailable(false);
                }
                partnerRepository.save(partner);

                outboxService.saveEvent("Delivery", assignment.getId(), "DeliveryPartnerAssignedEvent", Map.of(
                        "assignmentId", assignment.getId(),
                        "orderId", order.getId(),
                        "orderNumber", order.getOrderNumber(),
                        "partnerId", partner.getId(),
                        "partnerName", partner.getUser().getName()
                ));

                batchedCount++;
                log.info("Order {} successfully batched and assigned to driver {}", order.getOrderNumber(), partner.getUser().getName());
            }
        }

        return batchedCount;
    }
}
