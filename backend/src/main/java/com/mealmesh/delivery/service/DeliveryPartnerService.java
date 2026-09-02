package com.mealmesh.delivery.service;

import com.mealmesh.common.exception.BadRequestException;
import com.mealmesh.common.exception.ResourceNotFoundException;
import com.mealmesh.delivery.dto.DeliveryAssignmentResponse;
import com.mealmesh.delivery.dto.DeliveryPartnerProfileResponse;
import com.mealmesh.delivery.dto.PartnerStatusUpdateRequest;
import com.mealmesh.delivery.entity.DeliveryAssignment;
import com.mealmesh.delivery.entity.DeliveryPartner;
import com.mealmesh.delivery.repository.DeliveryAssignmentRepository;
import com.mealmesh.delivery.repository.DeliveryPartnerRepository;
import com.mealmesh.order.entity.Order;
import com.mealmesh.order.entity.OrderStatus;
import com.mealmesh.order.repository.OrderRepository;
import com.mealmesh.outbox.service.OutboxService;
import com.mealmesh.user.entity.User;
import com.mealmesh.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryPartnerService {

    private final DeliveryPartnerRepository partnerRepository;
    private final DeliveryAssignmentRepository assignmentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OutboxService outboxService;

    @Transactional(readOnly = true)
    public DeliveryPartnerProfileResponse getPartnerProfile(UUID userId) {
        DeliveryPartner partner = findPartnerByUserId(userId);
        return DeliveryPartnerProfileResponse.fromEntity(partner);
    }

    @Transactional
    public DeliveryPartnerProfileResponse updatePartnerStatus(UUID userId, PartnerStatusUpdateRequest request) {
        DeliveryPartner partner = findPartnerByUserId(userId);
        partner.setIsOnline(request.getIsOnline());
        if (request.getIsAvailable() != null) {
            partner.setIsAvailable(request.getIsAvailable());
        } else if (!request.getIsOnline()) {
            partner.setIsAvailable(false);
        }
        partner = partnerRepository.save(partner);
        log.info("Delivery partner status updated: userId={}, isOnline={}, isAvailable={}",
                userId, partner.getIsOnline(), partner.getIsAvailable());
        return DeliveryPartnerProfileResponse.fromEntity(partner);
    }

    @Transactional(readOnly = true)
    public Page<DeliveryAssignmentResponse> getPartnerAssignments(UUID userId, List<String> statuses, Pageable pageable) {
        DeliveryPartner partner = findPartnerByUserId(userId);
        if (statuses != null && !statuses.isEmpty()) {
            return assignmentRepository.findByDeliveryPartnerIdAndStatusInOrderByAssignedAtDesc(partner.getId(), statuses, pageable)
                    .map(DeliveryAssignmentResponse::fromEntity);
        }
        return assignmentRepository.findByDeliveryPartnerIdOrderByAssignedAtDesc(partner.getId(), pageable)
                .map(DeliveryAssignmentResponse::fromEntity);
    }

    @Transactional
    public DeliveryAssignmentResponse acceptAssignment(UUID userId, UUID assignmentId) {
        DeliveryPartner partner = findPartnerByUserId(userId);
        DeliveryAssignment assignment = findAssignmentById(assignmentId);

        if (!assignment.getDeliveryPartner().getId().equals(partner.getId())) {
            throw new BadRequestException("You are not authorized to accept this assignment");
        }

        if (!"ASSIGNED".equalsIgnoreCase(assignment.getStatus())) {
            throw new BadRequestException("Assignment cannot be accepted in its current status: " + assignment.getStatus());
        }

        assignment.setStatus("ACCEPTED");
        assignment.setAcceptedAt(Instant.now());
        assignment = assignmentRepository.save(assignment);

        log.info("Delivery partner accepted assignment: partnerId={}, assignmentId={}", partner.getId(), assignmentId);

        // Emit outbox event
        outboxService.saveEvent("Delivery", assignment.getId(), "DeliveryAcceptedEvent",
                Map.of(
                        "assignmentId", assignment.getId(),
                        "orderId", assignment.getOrder().getId(),
                        "deliveryPartnerId", partner.getId(),
                        "acceptedAt", Instant.now().toString()
                ));

        return DeliveryAssignmentResponse.fromEntity(assignment);
    }

    @Transactional
    public DeliveryAssignmentResponse rejectAssignment(UUID userId, UUID assignmentId, String reason) {
        DeliveryPartner partner = findPartnerByUserId(userId);
        DeliveryAssignment assignment = findAssignmentById(assignmentId);

        if (!assignment.getDeliveryPartner().getId().equals(partner.getId())) {
            throw new BadRequestException("You are not authorized to reject this assignment");
        }

        assignment.setStatus("REJECTED");
        assignment.setRejectionReason(reason);
        assignment = assignmentRepository.save(assignment);

        // Decrement partner's active order count
        if (partner.getCurrentActiveOrders() > 0) {
            partner.setCurrentActiveOrders(partner.getCurrentActiveOrders() - 1);
            partner.setIsAvailable(true);
            partnerRepository.save(partner);
        }

        log.info("Delivery partner rejected assignment: partnerId={}, assignmentId={}, reason={}",
                partner.getId(), assignmentId, reason);

        // Emit outbox event for reassignment trigger
        outboxService.saveEvent("Delivery", assignment.getId(), "DeliveryRejectedEvent",
                Map.of(
                        "assignmentId", assignment.getId(),
                        "orderId", assignment.getOrder().getId(),
                        "deliveryPartnerId", partner.getId(),
                        "reason", reason != null ? reason : "Partner rejected",
                        "rejectedAt", Instant.now().toString()
                ));

        return DeliveryAssignmentResponse.fromEntity(assignment);
    }

    @Transactional
    public DeliveryAssignmentResponse pickupOrder(UUID userId, UUID assignmentId) {
        DeliveryPartner partner = findPartnerByUserId(userId);
        DeliveryAssignment assignment = findAssignmentById(assignmentId);

        if (!assignment.getDeliveryPartner().getId().equals(partner.getId())) {
            throw new BadRequestException("You are not authorized for this assignment");
        }

        assignment.setStatus("PICKED_UP");
        assignment.setPickedUpAt(Instant.now());
        assignment = assignmentRepository.save(assignment);

        // Update Order status
        Order order = assignment.getOrder();
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);

        log.info("Delivery partner picked up order: partnerId={}, orderId={}", partner.getId(), order.getId());

        // Emit outbox event
        outboxService.saveEvent("Order", order.getId(), "OrderPickedUpEvent",
                Map.of(
                        "orderId", order.getId(),
                        "orderNumber", order.getOrderNumber(),
                        "deliveryPartnerId", partner.getId(),
                        "pickedUpAt", Instant.now().toString()
                ));

        return DeliveryAssignmentResponse.fromEntity(assignment);
    }

    @Transactional
    public DeliveryAssignmentResponse completeDelivery(UUID userId, UUID assignmentId) {
        DeliveryPartner partner = findPartnerByUserId(userId);
        DeliveryAssignment assignment = findAssignmentById(assignmentId);

        if (!assignment.getDeliveryPartner().getId().equals(partner.getId())) {
            throw new BadRequestException("You are not authorized for this assignment");
        }

        BigDecimal deliveryEarnings = BigDecimal.valueOf(50.00); // Standard base payout per delivery
        assignment.setStatus("DELIVERED");
        assignment.setDeliveredAt(Instant.now());
        assignment.setEarnings(deliveryEarnings);
        assignment = assignmentRepository.save(assignment);

        // Update Order status
        Order order = assignment.getOrder();
        order.setStatus(OrderStatus.DELIVERED);
        order.setActualDeliveryTime(Instant.now());
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);

        // Update partner delivery stats
        partner.setTotalDeliveries(partner.getTotalDeliveries() + 1);
        partner.setTotalEarnings(partner.getTotalEarnings().add(deliveryEarnings));
        if (partner.getCurrentActiveOrders() > 0) {
            partner.setCurrentActiveOrders(partner.getCurrentActiveOrders() - 1);
        }
        if (partner.getCurrentActiveOrders() < partner.getMaxConcurrentOrders() && Boolean.TRUE.equals(partner.getIsOnline())) {
            partner.setIsAvailable(true);
        }
        partnerRepository.save(partner);

        log.info("Delivery completed: partnerId={}, orderId={}, earnings={}",
                partner.getId(), order.getId(), deliveryEarnings);

        // Emit outbox event
        outboxService.saveEvent("Order", order.getId(), "OrderDeliveredEvent",
                Map.of(
                        "orderId", order.getId(),
                        "orderNumber", order.getOrderNumber(),
                        "deliveryPartnerId", partner.getId(),
                        "deliveredAt", Instant.now().toString()
                ));

        return DeliveryAssignmentResponse.fromEntity(assignment);
    }

    public DeliveryPartner findPartnerByUserId(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return partnerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery partner profile not found for user: " + userId));
    }

    private DeliveryAssignment findAssignmentById(UUID assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery assignment not found with id: " + assignmentId));
    }
}
