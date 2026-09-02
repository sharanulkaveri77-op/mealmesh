package com.mealmesh.outbox.publisher;

import com.mealmesh.outbox.entity.OutboxEvent;
import com.mealmesh.outbox.entity.OutboxStatus;
import com.mealmesh.outbox.repository.OutboxEventRepository;
import com.mealmesh.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Scheduled background worker that polls the outbox_events table for
 * PENDING and retryable FAILED events, publishes them to Kafka,
 * and updates their status accordingly.
 *
 * Workflow:
 *   Business transaction → DB update + Outbox event saved → Transaction commits
 *   → OutboxPublisher polls → Kafka publish → Mark event PUBLISHED
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxService outboxService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.outbox.batch-size:50}")
    private int batchSize;

    @Value("${app.outbox.max-retries:5}")
    private int maxRetries;

    /**
     * Maps event types to their Kafka topic names.
     */
    private static final Map<String, String> EVENT_TYPE_TO_TOPIC = Map.ofEntries(
            // Order events
            Map.entry("OrderCreatedEvent", "mealmesh.orders"),
            Map.entry("OrderStatusChangedEvent", "mealmesh.order-status"),
            Map.entry("OrderPreparingEvent", "mealmesh.order-status"),
            Map.entry("OrderReadyForPickupEvent", "mealmesh.order-status"),
            Map.entry("OrderPickedUpEvent", "mealmesh.order-status"),
            Map.entry("OrderOutForDeliveryEvent", "mealmesh.order-status"),
            Map.entry("OrderDeliveredEvent", "mealmesh.order-status"),
            // Payment events
            Map.entry("PaymentInitiatedEvent", "mealmesh.payments"),
            Map.entry("PaymentCompletedEvent", "mealmesh.payments"),
            Map.entry("PaymentFailedEvent", "mealmesh.payments"),
            // Restaurant events
            Map.entry("RestaurantOrderAcceptedEvent", "mealmesh.restaurant"),
            Map.entry("RestaurantOrderRejectedEvent", "mealmesh.restaurant"),
            // Delivery events
            Map.entry("DeliveryPartnerAssignedEvent", "mealmesh.delivery"),
            Map.entry("DeliveryLocationUpdateEvent", "mealmesh.delivery"),
            // Notification events
            Map.entry("NotificationEvent", "mealmesh.notifications")
    );

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:2000}")
    public void pollAndPublish() {
        List<OutboxEvent> events = outboxEventRepository.findPublishableEvents(
                maxRetries, PageRequest.of(0, batchSize));

        if (events.isEmpty()) {
            return;
        }

        log.debug("Outbox publisher polling: found {} events to publish", events.size());

        for (OutboxEvent event : events) {
            publishEvent(event);
        }
    }

    private void publishEvent(OutboxEvent event) {
        String topic = resolveTopicForEvent(event.getEventType());
        if (topic == null) {
            String error = "No Kafka topic mapped for event type: " + event.getEventType();
            log.error(error);
            outboxService.markAsFailed(event.getId(), error);
            return;
        }

        String key = event.getAggregateId().toString();

        try {
            kafkaTemplate.send(topic, key, event.getPayload())
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish outbox event {} to topic {}: {}",
                                    event.getId(), topic, ex.getMessage());
                            outboxService.markAsFailed(event.getId(), ex.getMessage());
                        } else {
                            log.info("Outbox event published: id={}, topic={}, partition={}, offset={}",
                                    event.getId(), topic,
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                            outboxService.markAsPublished(event.getId());
                        }
                    });
        } catch (Exception e) {
            log.error("Exception sending outbox event {} to Kafka: {}", event.getId(), e.getMessage(), e);
            outboxService.markAsFailed(event.getId(), e.getMessage());
        }
    }

    /**
     * Resolves the Kafka topic for a given event type.
     * Falls back to aggregate-type-based topic if no explicit mapping exists.
     */
    String resolveTopicForEvent(String eventType) {
        String topic = EVENT_TYPE_TO_TOPIC.get(eventType);
        if (topic != null) {
            return topic;
        }
        // Fallback: derive from event type naming convention
        if (eventType.startsWith("Order")) return "mealmesh.orders";
        if (eventType.startsWith("Payment")) return "mealmesh.payments";
        if (eventType.startsWith("Delivery")) return "mealmesh.delivery";
        if (eventType.startsWith("Restaurant")) return "mealmesh.restaurant";
        if (eventType.startsWith("Notification")) return "mealmesh.notifications";
        return null;
    }
}
