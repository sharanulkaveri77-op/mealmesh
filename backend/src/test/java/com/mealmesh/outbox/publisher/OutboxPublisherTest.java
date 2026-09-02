package com.mealmesh.outbox.publisher;

import com.mealmesh.outbox.entity.OutboxEvent;
import com.mealmesh.outbox.entity.OutboxStatus;
import com.mealmesh.outbox.repository.OutboxEventRepository;
import com.mealmesh.outbox.service.OutboxService;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private OutboxService outboxService;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private OutboxPublisher outboxPublisher;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(outboxPublisher, "batchSize", 50);
        ReflectionTestUtils.setField(outboxPublisher, "maxRetries", 5);
    }

    @Test
    @DisplayName("pollAndPublish should do nothing when no events found")
    void pollAndPublish_noEvents() {
        // Arrange
        when(outboxEventRepository.findPublishableEvents(anyInt(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        outboxPublisher.pollAndPublish();

        // Assert
        verifyNoInteractions(kafkaTemplate);
        verifyNoInteractions(outboxService);
    }

    @Test
    @DisplayName("pollAndPublish should publish event to Kafka and mark as published")
    void pollAndPublish_shouldPublishAndMark() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();
        OutboxEvent event = OutboxEvent.builder()
                .id(eventId)
                .aggregateType("Order")
                .aggregateId(aggregateId)
                .eventType("OrderCreatedEvent")
                .payload("{\"orderId\":\"" + aggregateId + "\"}")
                .status(OutboxStatus.PENDING)
                .retryCount(0)
                .createdAt(Instant.now())
                .build();

        when(outboxEventRepository.findPublishableEvents(anyInt(), any()))
                .thenReturn(List.of(event));

        // Mock successful Kafka send
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition("mealmesh.orders", 0), 0, 0, 0, 0, 0);
        SendResult<String, Object> sendResult = new SendResult<>(
                new ProducerRecord<>("mealmesh.orders", aggregateId.toString(), event.getPayload()),
                metadata);
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        // Act
        outboxPublisher.pollAndPublish();

        // Assert
        verify(kafkaTemplate).send(eq("mealmesh.orders"), eq(aggregateId.toString()), eq(event.getPayload()));
        verify(outboxService).markAsPublished(eventId);
    }

    @Test
    @DisplayName("pollAndPublish should mark as failed when Kafka send fails")
    void pollAndPublish_shouldMarkFailedOnKafkaError() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();
        OutboxEvent event = OutboxEvent.builder()
                .id(eventId)
                .aggregateType("Payment")
                .aggregateId(aggregateId)
                .eventType("PaymentCompletedEvent")
                .payload("{\"paymentId\":\"" + aggregateId + "\"}")
                .status(OutboxStatus.PENDING)
                .retryCount(0)
                .createdAt(Instant.now())
                .build();

        when(outboxEventRepository.findPublishableEvents(anyInt(), any()))
                .thenReturn(List.of(event));

        // Mock failed Kafka send
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka broker unavailable"));
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        // Act
        outboxPublisher.pollAndPublish();

        // Assert
        verify(outboxService).markAsFailed(eq(eventId), contains("Kafka broker unavailable"));
    }

    @Test
    @DisplayName("resolveTopicForEvent should map known event types correctly")
    void resolveTopicForEvent_shouldMapKnownEvents() {
        assertThat(outboxPublisher.resolveTopicForEvent("OrderCreatedEvent")).isEqualTo("mealmesh.orders");
        assertThat(outboxPublisher.resolveTopicForEvent("PaymentCompletedEvent")).isEqualTo("mealmesh.payments");
        assertThat(outboxPublisher.resolveTopicForEvent("DeliveryPartnerAssignedEvent")).isEqualTo("mealmesh.delivery");
        assertThat(outboxPublisher.resolveTopicForEvent("NotificationEvent")).isEqualTo("mealmesh.notifications");
    }

    @Test
    @DisplayName("resolveTopicForEvent should return null for unknown types")
    void resolveTopicForEvent_shouldReturnNullForUnknown() {
        assertThat(outboxPublisher.resolveTopicForEvent("CompletelyUnknownEvent")).isNull();
    }

    @Test
    @DisplayName("pollAndPublish should mark as failed when no topic mapping found")
    void pollAndPublish_shouldMarkFailedForUnmappedEvent() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        OutboxEvent event = OutboxEvent.builder()
                .id(eventId)
                .aggregateType("Unknown")
                .aggregateId(UUID.randomUUID())
                .eventType("CompletelyUnknownEvent")
                .payload("{}")
                .status(OutboxStatus.PENDING)
                .retryCount(0)
                .createdAt(Instant.now())
                .build();

        when(outboxEventRepository.findPublishableEvents(anyInt(), any()))
                .thenReturn(List.of(event));

        // Act
        outboxPublisher.pollAndPublish();

        // Assert
        verifyNoInteractions(kafkaTemplate);
        verify(outboxService).markAsFailed(eq(eventId), contains("No Kafka topic mapped"));
    }
}
