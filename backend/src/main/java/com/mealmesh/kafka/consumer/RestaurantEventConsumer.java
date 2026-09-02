package com.mealmesh.kafka.consumer;

import com.mealmesh.kafka.event.*;
import com.mealmesh.notification.service.NotificationService;
import com.mealmesh.delivery.service.DeliveryMatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantEventConsumer {

    private final NotificationService notificationService;
    private final DeliveryMatchingService deliveryMatchingService;

    @KafkaListener(topics = "${spring.kafka.topic.restaurant:mealmesh.restaurant}", groupId = "${spring.kafka.consumer.group-id:mealmesh-group}")
    public void handleRestaurantOrderAccepted(RestaurantOrderAcceptedEvent event, Acknowledgment ack) {
        try {
            log.info("Received RestaurantOrderAcceptedEvent: orderId={}", event.getOrderId());
            
            notificationService.sendOrderAcceptedNotification(event.getOrderId(), event.getPreparationTimeMinutes());
            
            // Trigger delivery partner matching
            deliveryMatchingService.matchAndAssignDeliveryPartner(event.getOrderId());
            
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing RestaurantOrderAcceptedEvent: {}", event.getEventId(), e);
            throw e;
        }
    }

    @KafkaListener(topics = "${spring.kafka.topic.restaurant:mealmesh.restaurant}", groupId = "${spring.kafka.consumer.group-id:mealmesh-group}")
    public void handleRestaurantOrderRejected(RestaurantOrderRejectedEvent event, Acknowledgment ack) {
        try {
            log.info("Received RestaurantOrderRejectedEvent: orderId={}", event.getOrderId());
            
            notificationService.sendOrderRejectedNotification(event.getOrderId(), event.getRejectionReason());
            
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing RestaurantOrderRejectedEvent: {}", event.getEventId(), e);
            throw e;
        }
    }
}