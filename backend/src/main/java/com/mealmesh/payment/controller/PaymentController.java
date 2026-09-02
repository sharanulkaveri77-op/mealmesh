package com.mealmesh.payment.controller;

import com.mealmesh.payment.dto.PaymentRequest;
import com.mealmesh.payment.dto.PaymentResponse;
import com.mealmesh.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponse> initiatePayment(
            @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.initiatePayment(request));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.getPayment(paymentId).toResponse());
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable UUID paymentId,
            @RequestParam BigDecimal amount,
            @RequestParam String reason) {
        return ResponseEntity.ok(paymentService.refundPayment(paymentId, amount, reason));
    }

    @PostMapping("/webhook/{providerName}")
    public ResponseEntity<PaymentResponse> handleWebhook(
            @PathVariable String providerName,
            @RequestBody String payload,
            @RequestHeader("X-Signature") String signature) {
        return ResponseEntity.ok(paymentService.handleWebhook(providerName, payload, signature));
    }

    @PostMapping("/callback/{paymentId}")
    public ResponseEntity<PaymentResponse> processCallback(
            @PathVariable UUID paymentId,
            @RequestParam String providerTransactionId,
            @RequestParam String status) {
        return ResponseEntity.ok(paymentService.processPaymentCallback(paymentId, providerTransactionId, status));
    }
}