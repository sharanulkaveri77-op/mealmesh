package com.mealmesh.kafka.producer;

import com.mealmesh.kafka.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.orders:mealmesh.orders}")
    private String ordersTopic;

    @Value("${spring.kafka.topic.payments:mealmesh.payments}")
    private String paymentsTopic;

    @Value("${spring.kafka.topic.restaurant:mealmesh.restaurant}")
    private String restaurantTopic;

    @Value("${spring.kafka.topic.delivery:mealmesh.delivery}")
    private String deliveryTopic;

    @Value("${spring.kafka.topic.notifications:mealmesh.notifications}")
    private String notificationsTopic;

    @Value("${spring.kafka.topic.order-status:mealmesh.order-status}")
    private String orderStatusTopic;

    public CompletableFuture<SendResult<String, Object>> sendOrderCreated(OrderCreatedEvent event) {
        return send(ordersTopic, event.getOrderId().toString(), event);
    }

    public CompletableFuture<SendResult<String, Object>> sendPaymentInitiated(PaymentInitiatedEvent event) {
        return send(paymentsTopic, event.getOrderId().toString(), event);
    }

    public CompletableFuture<SendResult<String, Object>> sendPaymentCompleted(PaymentCompletedEvent event) {
        return send(paymentsTopic, event.getOrderId().toString(), event);
    }

    public CompletableFuture<SendResult<String, Object>> sendPaymentFailed(PaymentFailedEvent event) {
        return send(paymentsTopic, event.getOrderId().toString(), event);
    }

    public CompletableFuture<SendResult<String, Object>> sendOrderStatusChanged(OrderStatusChangedEvent event) {
        return send(orderStatusTopic, event.getOrderId().toString(), event);
    }

    public CompletableFuture<SendResult<String, Object>> sendRestaurantOrderAccepted(RestaurantOrderAcceptedEvent event) {
        return send(restaurantTopic, event.getOrderId().toString(), event);
    }

    public CompletableFuture<SendResult<String, Object>> sendRestaurantOrderRejected(RestaurantOrderRejectedEvent event) {
        return send(restaurantTopic, event.getOrderId().toString(), event);
    }

    public CompletableFuture<SendResult<String, Object>> sendOrderPreparing(OrderPreparingEvent event) {
        return send(orderStatusTopic, event.getOrderId().toString(), event);
    }

    public CompletableFuture<SendResult<String, Object>> sendOrderReadyForPickup(OrderReadyForPickupEvent event) {
        return send(orderStatusTopic, event.getOrderId().toString(), event);
    }

    public CompletableFuture<SendResult<String, Object>> sendDeliveryPartnerAssigned(DeliveryPartnerAssignedEvent event) {
        return send(deliveryTopic, event.getOrderId().toString(), event);
    }

    public CompletableFuture<SendResult<String, Object>> sendOrderPickedUp(OrderPickedUpEvent event) {
        return send(orderStatusTopic, event.getOrderId().toString(), event);
    }

    public CompletableFuture<SendResult<String, Object>> sendOrderOutForDelivery(OrderOutForDeliveryEvent event) {
        return send(orderStatusTopic, event.getOrderId().toString(), event);
    }

    public CompletableFuture<SendResult<String, Object>> sendOrderDelivered(OrderDeliveredEvent event) {
        return send(orderStatusTopic, event.getOrderId().toString(), event);
    }

    public CompletableFuture<SendResult<String, Object>> sendNotification(NotificationEvent event) {
        return send(notificationsTopic, event.getUserId().toString(), event);
    }

    private CompletableFuture<SendResult<String, Object>> send(String topic, String key, Object event) {
        log.debug("Sending event to topic: {}, key: {}", topic, key);
        return kafkaTemplate.send(topic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send event to topic: {}, key: {}", topic, key, ex);
                    } else {
                        log.debug("Event sent successfully to topic: {}, partition: {}, offset: {}",
                                topic, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                    }
                });
    }
}