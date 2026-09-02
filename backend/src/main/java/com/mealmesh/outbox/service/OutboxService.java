package com.mealmesh.outbox.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mealmesh.outbox.entity.OutboxEvent;
import com.mealmesh.outbox.entity.OutboxStatus;
import com.mealmesh.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Service responsible for atomically persisting outbox events within
 * the same database transaction as the business operation.
 * <p>
 * This guarantees that either the business state AND the event are saved,
 * or neither is — eliminating dual-write inconsistency.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    /**
     * Save an outbox event within the current transaction.
     * Must be called from within an active @Transactional boundary.
     *
     * @param aggregateType e.g. "Order", "Payment", "Delivery"
     * @param aggregateId   the ID of the aggregate root
     * @param eventType     e.g. "OrderCreatedEvent", "PaymentCompletedEvent"
     * @param eventPayload  the event object — will be serialized to JSON
     * @return the persisted OutboxEvent
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public OutboxEvent saveEvent(String aggregateType, UUID aggregateId,
                                  String eventType, Object eventPayload) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(eventPayload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize outbox event payload: aggregateType={}, eventType={}",
                    aggregateType, eventType, e);
            throw new RuntimeException("Failed to serialize outbox event payload", e);
        }

        OutboxEvent event = OutboxEvent.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(payload)
                .status(OutboxStatus.PENDING)
                .retryCount(0)
                .createdAt(Instant.now())
                .build();

        event = outboxEventRepository.save(event);
        log.debug("Outbox event saved: id={}, aggregateType={}, eventType={}",
                event.getId(), aggregateType, eventType);
        return event;
    }

    /**
     * Mark an outbox event as successfully published.
     */
    @Transactional
    public void markAsPublished(UUID eventId) {
        outboxEventRepository.findById(eventId).ifPresent(event -> {
            event.setStatus(OutboxStatus.PUBLISHED);
            event.setPublishedAt(Instant.now());
            event.setProcessedAt(Instant.now());
            outboxEventRepository.save(event);
            log.debug("Outbox event published: id={}, eventType={}", eventId, event.getEventType());
        });
    }

    /**
     * Record a publish failure and increment retry count.
     * If retryCount exceeds the threshold, the publisher stops retrying.
     */
    @Transactional
    public void markAsFailed(UUID eventId, String errorMessage) {
        outboxEventRepository.findById(eventId).ifPresent(event -> {
            event.setRetryCount(event.getRetryCount() + 1);
            event.setLastError(errorMessage);
            event.setStatus(OutboxStatus.FAILED);
            outboxEventRepository.save(event);
            log.warn("Outbox event failed: id={}, retryCount={}, error={}",
                    eventId, event.getRetryCount(), errorMessage);
        });
    }
}
