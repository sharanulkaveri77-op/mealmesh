package com.mealmesh.kafka.consumer;

import com.mealmesh.kafka.event.*;
import com.mealmesh.payment.service.PaymentService;
import com.mealmesh.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final PaymentService paymentService;
    private final NotificationService notificationService;

    @KafkaListener(topics = "${spring.kafka.topic.payments:mealmesh.payments}", groupId = "${spring.kafka.consumer.group-id:mealmesh-group}")
    public void handlePaymentCompleted(PaymentCompletedEvent event, Acknowledgment ack) {
        try {
            log.info("Received PaymentCompletedEvent: paymentId={}, orderId={}", event.getPaymentId(), event.getOrderId());
            
            // Update payment status
            paymentService.processPaymentCallback(event.getPaymentId(), event.getProviderTransactionId(), "COMPLETED");
            
            // Send notification to customer
            notificationService.sendPaymentSuccessNotification(event.getCustomerId(), event.getOrderNumber());
            
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing PaymentCompletedEvent: {}", event.getEventId(), e);
            throw e;
        }
    }

    @KafkaListener(topics = "${spring.kafka.topic.payments:mealmesh.payments}", groupId = "${spring.kafka.consumer.group-id:mealmesh-group}")
    public void handlePaymentFailed(PaymentFailedEvent event, Acknowledgment ack) {
        try {
            log.info("Received PaymentFailedEvent: paymentId={}, orderId={}", event.getPaymentId(), event.getOrderId());
            
            // Update payment status
            paymentService.processPaymentCallback(event.getPaymentId(), null, "FAILED");
            
            // Send notification to customer
            notificationService.sendPaymentFailedNotification(event.getCustomerId(), event.getOrderNumber(), event.getFailureReason());
            
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing PaymentFailedEvent: {}", event.getEventId(), e);
            throw e;
        }
    }
}