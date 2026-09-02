package com.mealmesh.delivery.controller;

import com.mealmesh.delivery.dto.*;
import com.mealmesh.delivery.service.DeliveryPartnerService;
import com.mealmesh.delivery.service.DeliveryTrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryPartnerService deliveryPartnerService;
    private final DeliveryTrackingService deliveryTrackingService;

    @GetMapping("/track/{orderId}")
    public ResponseEntity<DeliveryTrackingResponse> trackOrder(@PathVariable UUID orderId) {
        DeliveryTrackingResponse response = deliveryTrackingService.trackOrder(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/partner/profile")
    public ResponseEntity<DeliveryPartnerProfileResponse> getPartnerProfile(
            @AuthenticationPrincipal UUID userId) {
        DeliveryPartnerProfileResponse profile = deliveryPartnerService.getPartnerProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @PatchMapping("/partner/status")
    public ResponseEntity<DeliveryPartnerProfileResponse> updatePartnerStatus(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody PartnerStatusUpdateRequest request) {
        DeliveryPartnerProfileResponse profile = deliveryPartnerService.updatePartnerStatus(userId, request);
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/partner/location")
    public ResponseEntity<Map<String, String>> updatePartnerLocation(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody LocationUpdateRequest request) {
        deliveryTrackingService.recordLocation(userId, request);
        return ResponseEntity.ok(Map.of("message", "Location updated successfully"));
    }

    @GetMapping("/partner/assignments")
    public ResponseEntity<Page<DeliveryAssignmentResponse>> getPartnerAssignments(
            @AuthenticationPrincipal UUID userId,
            @RequestParam(required = false) List<String> statuses,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<DeliveryAssignmentResponse> assignments = deliveryPartnerService.getPartnerAssignments(userId, statuses, pageable);
        return ResponseEntity.ok(assignments);
    }

    @PostMapping("/partner/assignments/{assignmentId}/accept")
    public ResponseEntity<DeliveryAssignmentResponse> acceptAssignment(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID assignmentId) {
        DeliveryAssignmentResponse response = deliveryPartnerService.acceptAssignment(userId, assignmentId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/partner/assignments/{assignmentId}/reject")
    public ResponseEntity<DeliveryAssignmentResponse> rejectAssignment(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID assignmentId,
            @RequestBody(required = false) AssignmentRejectRequest request) {
        String reason = request != null ? request.getReason() : "Partner rejected";
        DeliveryAssignmentResponse response = deliveryPartnerService.rejectAssignment(userId, assignmentId, reason);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/partner/assignments/{assignmentId}/pickup")
    public ResponseEntity<DeliveryAssignmentResponse> pickupOrder(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID assignmentId) {
        DeliveryAssignmentResponse response = deliveryPartnerService.pickupOrder(userId, assignmentId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/partner/assignments/{assignmentId}/deliver")
    public ResponseEntity<DeliveryAssignmentResponse> completeDelivery(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID assignmentId) {
        DeliveryAssignmentResponse response = deliveryPartnerService.completeDelivery(userId, assignmentId);
        return ResponseEntity.ok(response);
    }
}
