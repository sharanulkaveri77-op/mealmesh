package com.mealmesh.kafka.consumer;

import com.mealmesh.kafka.event.*;
import com.mealmesh.notification.service.NotificationService;
import com.mealmesh.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final OrderService orderService;
    private final NotificationService notificationService;

    @KafkaListener(topics = "${spring.kafka.topic.orders:mealmesh.orders}", groupId = "${spring.kafka.consumer.group-id:mealmesh-group}")
    public void handleOrderCreated(OrderCreatedEvent event, Acknowledgment ack) {
        try {
            log.info("Received OrderCreatedEvent: orderId={}, orderNumber={}", event.getOrderId(), event.getOrderNumber());
            
            // Send notification to customer
            notificationService.sendOrderCreatedNotification(event.getCustomerId(), event.getOrderNumber());
            
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing OrderCreatedEvent: {}", event.getEventId(), e);
            throw e;
        }
    }

    @KafkaListener(topics = "${spring.kafka.topic.order-status:mealmesh.order-status}", groupId = "${spring.kafka.consumer.group-id:mealmesh-group}")
    public void handleOrderStatusChanged(OrderStatusChangedEvent event, Acknowledgment ack) {
        try {
            log.info("Received OrderStatusChangedEvent: orderId={}, {} -> {}", 
                    event.getOrderId(), event.getPreviousStatus(), event.getNewStatus());
            
            // Send notification to customer
            notificationService.sendOrderStatusNotification(
                    event.getOrderId(), 
                    event.getPreviousStatus(), 
                    event.getNewStatus()
            );
            
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing OrderStatusChangedEvent: {}", event.getEventId(), e);
            throw e;
        }
    }

    @KafkaListener(topics = "${spring.kafka.topic.order-status:mealmesh.order-status}", groupId = "${spring.kafka.consumer.group-id:mealmesh-group}")
    public void handleOrderReadyForPickup(OrderReadyForPickupEvent event, Acknowledgment ack) {
        try {
            log.info("Received OrderReadyForPickupEvent: orderId={}", event.getOrderId());
            notificationService.sendOrderReadyNotification(event.getOrderId());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing OrderReadyForPickupEvent: {}", event.getEventId(), e);
            throw e;
        }
    }

    @KafkaListener(topics = "${spring.kafka.topic.order-status:mealmesh.order-status}", groupId = "${spring.kafka.consumer.group-id:mealmesh-group}")
    public void handleOrderPickedUp(OrderPickedUpEvent event, Acknowledgment ack) {
        try {
            log.info("Received OrderPickedUpEvent: orderId={}", event.getOrderId());
            notificationService.sendOrderPickedUpNotification(event.getOrderId());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing OrderPickedUpEvent: {}", event.getEventId(), e);
            throw e;
        }
    }

    @KafkaListener(topics = "${spring.kafka.topic.order-status:mealmesh.order-status}", groupId = "${spring.kafka.consumer.group-id:mealmesh-group}")
    public void handleOrderOutForDelivery(OrderOutForDeliveryEvent event, Acknowledgment ack) {
        try {
            log.info("Received OrderOutForDeliveryEvent: orderId={}", event.getOrderId());
            notificationService.sendOrderOutForDeliveryNotification(event.getOrderId());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing OrderOutForDeliveryEvent: {}", event.getEventId(), e);
            throw e;
        }
    }

    @KafkaListener(topics = "${spring.kafka.topic.order-status:mealmesh.order-status}", groupId = "${spring.kafka.consumer.group-id:mealmesh-group}")
    public void handleOrderDelivered(OrderDeliveredEvent event, Acknowledgment ack) {
        try {
            log.info("Received OrderDeliveredEvent: orderId={}", event.getOrderId());
            notificationService.sendOrderDeliveredNotification(event.getCustomerId(), event.getOrderNumber());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing OrderDeliveredEvent: {}", event.getEventId(), e);
            throw e;
        }
    }
}