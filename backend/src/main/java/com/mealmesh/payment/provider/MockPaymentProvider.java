package com.mealmesh.payment.provider;

import com.mealmesh.payment.dto.PaymentRequest;
import com.mealmesh.payment.dto.PaymentResponse;
import com.mealmesh.payment.entity.Payment;
import com.mealmesh.payment.entity.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@Slf4j
public class MockPaymentProvider implements PaymentProvider {

    @Override
    public String getProviderName() {
        return "MOCK";
    }

    @Override
    public PaymentResponse initiatePayment(PaymentRequest request) {
        log.info("Mock payment initiated for order: {}", request.getOrderId());
        
        String transactionId = "mock_txn_" + UUID.randomUUID().toString().substring(0, 8);
        String orderId = "mock_order_" + UUID.randomUUID().toString().substring(0, 8);
        
        // Simulate instant success for COD, pending for others
        PaymentStatus status = request.getPaymentMethod().equals("COD") ? PaymentStatus.COMPLETED : PaymentStatus.PROCESSING;
        
        return PaymentResponse.builder()
                .providerName(getProviderName())
                .providerTransactionId(transactionId)
                .providerOrderId(orderId)
                .status(status.name())
                .failureReason(null)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentUrl(status == PaymentStatus.PROCESSING ? "https://mock-payment-gateway.com/pay/" + transactionId : null)
                .webhookUrl(request.getWebhookUrl())
                .orderId(request.getOrderId())
                .idempotencyKey(request.getIdempotencyKey())
                .metadata("mock=true")
                .build();
    }

    @Override
    public PaymentResponse verifyPayment(String providerTransactionId) {
        log.info("Verifying mock payment: {}", providerTransactionId);
        
        return PaymentResponse.builder()
                .providerName(getProviderName())
                .providerTransactionId(providerTransactionId)
                .providerOrderId("mock_order_" + providerTransactionId.replace("mock_txn_", ""))
                .status(PaymentStatus.COMPLETED.name())
                .failureReason(null)
                .amount(BigDecimal.ZERO)
                .currency("INR")
                .build();
    }

    @Override
    public PaymentResponse refundPayment(String providerTransactionId, java.math.BigDecimal amount) {
        log.info("Refunding mock payment: {} for amount: {}", providerTransactionId, amount);
        
        return PaymentResponse.builder()
                .providerName(getProviderName())
                .providerTransactionId("mock_refund_" + UUID.randomUUID().toString().substring(0, 8))
                .providerOrderId(providerTransactionId)
                .status(PaymentStatus.REFUNDED.name())
                .failureReason(null)
                .amount(amount)
                .currency("INR")
                .build();
    }

    @Override
    public boolean isWebhookValid(String payload, String signature) {
        // Mock always valid
        return true;
    }
}