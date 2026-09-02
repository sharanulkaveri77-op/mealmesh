package com.mealmesh.payment.controller;

import com.mealmesh.payment.dto.RefundRequest;
import com.mealmesh.payment.dto.RefundResponse;
import com.mealmesh.payment.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    @PostMapping
    public ResponseEntity<RefundResponse> requestRefund(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody RefundRequest request) {
        return ResponseEntity.ok(refundService.processRefund(userId, request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<RefundResponse>> getMyRefunds(
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(refundService.getUserRefunds(userId));
    }
}
