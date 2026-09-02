package com.mealmesh.kafka.consumer;

import com.mealmesh.kafka.event.NotificationEvent;
import com.mealmesh.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "${spring.kafka.topic.notifications:mealmesh.notifications}", groupId = "${spring.kafka.consumer.group-id:mealmesh-group}")
    public void handleNotification(NotificationEvent event, Acknowledgment ack) {
        try {
            log.info("Received NotificationEvent: userId={}, type={}", event.getUserId(), event.getType());
            
            notificationService.createNotification(event.getUserId(), event.getType(), event.getTitle(), event.getMessage(), event.getData());
            
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing NotificationEvent: {}", event.getEventId(), e);
            throw e;
        }
    }
}