package com.mealmesh.payment.provider;

import com.mealmesh.payment.dto.PaymentRequest;
import com.mealmesh.payment.dto.PaymentResponse;
import com.mealmesh.payment.entity.Payment;

public interface PaymentProvider {

    String getProviderName();

    PaymentResponse initiatePayment(PaymentRequest request);

    PaymentResponse verifyPayment(String providerTransactionId);

    PaymentResponse refundPayment(String providerTransactionId, java.math.BigDecimal amount);

    boolean isWebhookValid(String payload, String signature);
}