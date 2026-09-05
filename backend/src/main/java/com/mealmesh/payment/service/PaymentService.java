package com.mealmesh.payment.service;

import com.mealmesh.common.exception.BadRequestException;
import com.mealmesh.common.exception.ResourceNotFoundException;
import com.mealmesh.order.entity.Order;
import com.mealmesh.order.entity.OrderStatus;
import com.mealmesh.order.repository.OrderRepository;
import com.mealmesh.payment.dto.PaymentRequest;
import com.mealmesh.payment.dto.PaymentResponse;
import com.mealmesh.payment.entity.Payment;
import com.mealmesh.payment.entity.PaymentStatus;
import com.mealmesh.payment.entity.RefundStatus;
import com.mealmesh.payment.provider.PaymentProvider;
import com.mealmesh.payment.provider.MockPaymentProvider;
import com.mealmesh.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mealmesh.outbox.service.OutboxService;
import com.mealmesh.audit.annotation.Auditable;
import com.mealmesh.audit.entity.AuditAction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OutboxService outboxService;
    private final Map<String, PaymentProvider> providers = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        registerProvider(new MockPaymentProvider());
    }

    public void registerProvider(PaymentProvider provider) {
        providers.put(provider.getProviderName(), provider);
    }

    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request) {
        // Idempotency check
        if (request.getIdempotencyKey() != null) {
            Optional<Payment> existing = paymentRepository.findByPaymentIdempotencyKey(request.getIdempotencyKey());
            if (existing.isPresent()) {
                log.info("Idempotent payment request: {}", request.getIdempotencyKey());
                Payment payment = existing.get();
                return toResponse(payment);
            }
        }

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Validate amount matches order
        if (request.getAmount().compareTo(order.getTotalAmount()) != 0) {
            throw new BadRequestException("Payment amount does not match order total");
        }

        // Create payment record with PENDING status
        Payment payment = Payment.builder()
                .order(order)
                .paymentIdempotencyKey(request.getIdempotencyKey() != null ? request.getIdempotencyKey() : UUID.randomUUID().toString())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentMethod(request.getPaymentMethod())
                .paymentProvider(selectProvider(request.getPaymentMethod()).getProviderName())
                .status(PaymentStatus.PENDING)
                .initiatedAt(Instant.now())
                .metadata("{}")
                .build();

        payment = paymentRepository.save(payment);

        // Initiate with provider
        PaymentProvider provider = selectProvider(request.getPaymentMethod());
        request.setWebhookUrl("http://localhost:8080/api/payment/webhook/" + provider.getProviderName());
        
        PaymentResponse providerResponse = provider.initiatePayment(request);

        // Update payment with provider response
        payment.setProviderTransactionId(providerResponse.getProviderTransactionId());
        payment.setProviderOrderId(providerResponse.getProviderOrderId());
        payment.setStatus(PaymentStatus.valueOf(providerResponse.getStatus()));
        
        if (providerResponse.getFailureReason() != null) {
            payment.setFailureReason(providerResponse.getFailureReason());
        }

        payment = paymentRepository.save(payment);

        // Update order payment status
        order.setPaymentStatus(payment.getStatus().name());
        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            order.setPaymentStatus("COMPLETED");
            order.setPaymentId(payment.getId());
        }

        log.info("Payment initiated: {} for order: {}", payment.getId(), order.getId());

        // Save PaymentInitiatedEvent to outbox
        outboxService.saveEvent("Payment", payment.getId(), "PaymentInitiatedEvent",
                java.util.Map.of(
                        "paymentId", payment.getId(),
                        "orderId", order.getId(),
                        "amount", payment.getAmount(),
                        "currency", payment.getCurrency(),
                        "paymentMethod", payment.getPaymentMethod(),
                        "status", payment.getStatus().name()
                ));

        return toResponse(payment);
    }

    @Transactional
    public PaymentResponse handleWebhook(String providerName, String payload, String signature) {
        PaymentProvider provider = providers.get(providerName);
        if (provider == null) {
            throw new BadRequestException("Unknown payment provider: " + providerName);
        }

        if (!provider.isWebhookValid(payload, signature)) {
            throw new BadRequestException("Invalid webhook signature");
        }

        // Parse webhook payload (implementation depends on provider format)
        // For mock, we'll assume it contains providerTransactionId and status
        // In real implementation, parse JSON and extract fields
        
        log.info("Webhook received for provider: {}", providerName);
        return PaymentResponse.builder().status("RECEIVED").build();
    }

    @Transactional
    @Auditable(entityType = "Payment", action = AuditAction.UPDATE, entityIdParam = "paymentId")
    public PaymentResponse processPaymentCallback(UUID paymentId, String providerTransactionId, String status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        PaymentProvider provider = providers.get(payment.getPaymentProvider());
        if (provider == null) {
            throw new BadRequestException("Provider not found: " + payment.getPaymentProvider());
        }

        PaymentResponse providerResponse = provider.verifyPayment(providerTransactionId);
        
        PaymentStatus newStatus = PaymentStatus.valueOf(providerResponse.getStatus());
        payment.setProviderTransactionId(providerResponse.getProviderTransactionId());
        payment.setProviderOrderId(providerResponse.getProviderOrderId());
        payment.setStatus(newStatus);
        
        if (providerResponse.getFailureReason() != null) {
            payment.setFailureReason(providerResponse.getFailureReason());
        }
        
        if (newStatus == PaymentStatus.COMPLETED) {
            payment.setCompletedAt(Instant.now());
        }

        payment = paymentRepository.save(payment);

        // Update order
        Order order = payment.getOrder();
        order.setPaymentStatus(payment.getStatus().name());
        if (newStatus == PaymentStatus.COMPLETED) {
            order.setPaymentStatus("COMPLETED");
            order.setPaymentId(payment.getId());
            order.setStatus(com.mealmesh.order.entity.OrderStatus.PAYMENT_CONFIRMED);
        } else if (newStatus == PaymentStatus.FAILED) {
            order.setPaymentStatus("FAILED");
            order.setStatus(com.mealmesh.order.entity.OrderStatus.PAYMENT_FAILED);
        }

        log.info("Payment callback processed: {} -> {}", paymentId, newStatus);

        // Save PaymentCompleted/FailedEvent to outbox
        String eventType = (newStatus == PaymentStatus.COMPLETED) ? "PaymentCompletedEvent" : "PaymentFailedEvent";
        outboxService.saveEvent("Payment", payment.getId(), eventType,
                java.util.Map.of(
                        "paymentId", payment.getId(),
                        "orderId", order.getId(),
                        "status", newStatus.name(),
                        "amount", payment.getAmount()
                ));

        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public Payment getPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByOrder(UUID orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(List::of)
                .orElse(List.of());
    }

    @Transactional
    public PaymentResponse refundPayment(UUID paymentId, BigDecimal amount, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new BadRequestException("Can only refund completed payments");
        }

        if (amount.compareTo(payment.getAmount()) > 0) {
            throw new BadRequestException("Refund amount cannot exceed payment amount");
        }

        PaymentProvider provider = providers.get(payment.getPaymentProvider());
        if (provider == null) {
            throw new BadRequestException("Provider not found: " + payment.getPaymentProvider());
        }

        PaymentResponse providerResponse = provider.refundPayment(payment.getProviderTransactionId(), amount);

        payment.setRefundedAmount(payment.getRefundedAmount().add(amount));
        payment.setRefundStatus(RefundStatus.PENDING);
        payment.setRefundReason(reason);
        
        if (payment.getRefundedAmount().compareTo(payment.getAmount()) >= 0) {
            payment.setRefundStatus(RefundStatus.COMPLETED);
            payment.setStatus(PaymentStatus.REFUNDED);
        } else {
            payment.setRefundStatus(RefundStatus.PENDING);
        }

        payment = paymentRepository.save(payment);
        
        log.info("Refund initiated for payment: {} amount: {}", paymentId, amount);
        return toResponse(payment);
    }

    private PaymentProvider selectProvider(String paymentMethod) {
        // In production, this would be more sophisticated
        return providers.get("MOCK");
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .providerName(payment.getPaymentProvider())
                .providerTransactionId(payment.getProviderTransactionId())
                .providerOrderId(payment.getProviderOrderId())
                .status(payment.getStatus().name())
                .failureReason(payment.getFailureReason())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .orderId(payment.getOrder().getId())
                .idempotencyKey(payment.getPaymentIdempotencyKey())
                .build();
    }
}