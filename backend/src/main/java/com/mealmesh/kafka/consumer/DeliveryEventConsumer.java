package com.mealmesh.kafka.consumer;

import com.mealmesh.kafka.event.*;
import com.mealmesh.notification.service.NotificationService;
import com.mealmesh.delivery.service.DeliveryTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryEventConsumer {

    private final NotificationService notificationService;
    private final DeliveryTrackingService deliveryTrackingService;

    @KafkaListener(topics = "${spring.kafka.topic.delivery:mealmesh.delivery}", groupId = "${spring.kafka.consumer.group-id:mealmesh-group}")
    public void handleDeliveryPartnerAssigned(DeliveryPartnerAssignedEvent event, Acknowledgment ack) {
        try {
            log.info("Received DeliveryPartnerAssignedEvent: orderId={}, partnerId={}", event.getOrderId(), event.getDeliveryPartnerId());
            
            notificationService.sendDeliveryPartnerAssignedNotification(event.getOrderId(), event.getPartnerName());
            
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing DeliveryPartnerAssignedEvent: {}", event.getEventId(), e);
            throw e;
        }
    }

    @KafkaListener(topics = "${spring.kafka.topic.delivery:mealmesh.delivery}", groupId = "${spring.kafka.consumer.group-id:mealmesh-group}")
    public void handleDeliveryLocationUpdate(DeliveryLocationUpdateEvent event, Acknowledgment ack) {
        try {
            log.debug("Received DeliveryLocationUpdateEvent: orderId={}, lat={}, lng={}", 
                    event.getOrderId(), event.getLatitude(), event.getLongitude());
            
            deliveryTrackingService.updateLocation(event.getOrderId(), event.getLatitude(), event.getLongitude());
            
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing DeliveryLocationUpdateEvent: {}", event.getEventId(), e);
            throw e;
        }
    }
}