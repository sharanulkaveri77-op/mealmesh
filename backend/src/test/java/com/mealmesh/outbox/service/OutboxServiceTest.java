package com.mealmesh.outbox.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mealmesh.outbox.entity.OutboxEvent;
import com.mealmesh.outbox.entity.OutboxStatus;
import com.mealmesh.outbox.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxServiceTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OutboxService outboxService;

    private UUID aggregateId;
    private Map<String, Object> testPayload;

    @BeforeEach
    void setUp() {
        aggregateId = UUID.randomUUID();
        testPayload = Map.of("orderId", aggregateId, "status", "CREATED");
    }

    @Test
    @DisplayName("saveEvent should persist a PENDING OutboxEvent with serialized payload")
    void saveEvent_shouldPersistPendingEvent() throws JsonProcessingException {
        // Arrange
        String serializedPayload = "{\"orderId\":\"" + aggregateId + "\",\"status\":\"CREATED\"}";
        when(objectMapper.writeValueAsString(testPayload)).thenReturn(serializedPayload);
        when(outboxEventRepository.save(any(OutboxEvent.class))).thenAnswer(inv -> {
            OutboxEvent e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        // Act
        OutboxEvent result = outboxService.saveEvent("Order", aggregateId, "OrderCreatedEvent", testPayload);

        // Assert
        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());
        OutboxEvent saved = captor.getValue();

        assertThat(saved.getAggregateType()).isEqualTo("Order");
        assertThat(saved.getAggregateId()).isEqualTo(aggregateId);
        assertThat(saved.getEventType()).isEqualTo("OrderCreatedEvent");
        assertThat(saved.getPayload()).isEqualTo(serializedPayload);
        assertThat(saved.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(saved.getRetryCount()).isEqualTo(0);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("saveEvent should throw RuntimeException when payload serialization fails")
    void saveEvent_shouldThrowWhenSerializationFails() throws JsonProcessingException {
        // Arrange
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("bad") {});

        // Act & Assert
        assertThatThrownBy(() -> outboxService.saveEvent("Order", aggregateId, "OrderCreatedEvent", testPayload))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to serialize outbox event payload");

        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("markAsPublished should update status to PUBLISHED and set timestamps")
    void markAsPublished_shouldUpdateStatusAndTimestamps() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        OutboxEvent event = OutboxEvent.builder()
                .id(eventId)
                .status(OutboxStatus.PENDING)
                .build();
        when(outboxEventRepository.findById(eventId)).thenReturn(Optional.of(event));

        // Act
        outboxService.markAsPublished(eventId);

        // Assert
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
        assertThat(event.getPublishedAt()).isNotNull();
        assertThat(event.getProcessedAt()).isNotNull();
        verify(outboxEventRepository).save(event);
    }

    @Test
    @DisplayName("markAsPublished should be a no-op for non-existent events")
    void markAsPublished_shouldNoOpForMissingEvent() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        when(outboxEventRepository.findById(eventId)).thenReturn(Optional.empty());

        // Act
        outboxService.markAsPublished(eventId);

        // Assert
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("markAsFailed should increment retry count and record error")
    void markAsFailed_shouldIncrementRetryAndRecordError() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        OutboxEvent event = OutboxEvent.builder()
                .id(eventId)
                .status(OutboxStatus.PENDING)
                .retryCount(1)
                .build();
        when(outboxEventRepository.findById(eventId)).thenReturn(Optional.of(event));

        // Act
        outboxService.markAsFailed(eventId, "Kafka broker unreachable");

        // Assert
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(event.getRetryCount()).isEqualTo(2);
        assertThat(event.getLastError()).isEqualTo("Kafka broker unreachable");
        verify(outboxEventRepository).save(event);
    }
}
