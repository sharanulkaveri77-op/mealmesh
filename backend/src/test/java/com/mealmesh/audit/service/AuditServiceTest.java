package com.mealmesh.audit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mealmesh.audit.dto.AuditLogResponse;
import com.mealmesh.audit.entity.AuditAction;
import com.mealmesh.audit.entity.AuditLog;
import com.mealmesh.audit.repository.AuditLogRepository;
import com.mealmesh.user.entity.User;
import com.mealmesh.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuditService auditService;

    private UUID entityId;
    private UUID actorId;
    private User testUser;

    @BeforeEach
    void setUp() {
        entityId = UUID.randomUUID();
        actorId = UUID.randomUUID();
        testUser = User.builder()
                .id(actorId)
                .name("Test User")
                .email("test@example.com")
                .build();
    }

    @Test
    @DisplayName("logAction should persist an AuditLog entry with correct details")
    void logAction_shouldPersistAuditLog() throws Exception {
        // Arrange
        Map<String, Object> oldValues = Map.of("status", "PENDING");
        Map<String, Object> newValues = Map.of("status", "CONFIRMED");

        when(userRepository.findById(actorId)).thenReturn(Optional.of(testUser));
        when(objectMapper.writeValueAsString(oldValues)).thenReturn("{\"status\":\"PENDING\"}");
        when(objectMapper.writeValueAsString(newValues)).thenReturn("{\"status\":\"CONFIRMED\"}");

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> {
            AuditLog log = inv.getArgument(0);
            log.setId(UUID.randomUUID());
            return log;
        });

        // Act
        AuditLog saved = auditService.logAction(
                "Order", entityId, AuditAction.STATUS_CHANGE,
                oldValues, newValues, "status", actorId
        );

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog captured = captor.getValue();

        assertThat(captured.getEntityType()).isEqualTo("Order");
        assertThat(captured.getEntityId()).isEqualTo(entityId);
        assertThat(captured.getAction()).isEqualTo(AuditAction.STATUS_CHANGE);
        assertThat(captured.getPerformedBy()).isEqualTo(testUser);
        assertThat(captured.getOldValues()).isEqualTo("{\"status\":\"PENDING\"}");
        assertThat(captured.getNewValues()).isEqualTo("{\"status\":\"CONFIRMED\"}");
        assertThat(captured.getChangedFields()).isEqualTo("status");
        assertThat(saved).isNotNull();
    }

    @Test
    @DisplayName("getAuditLogsForEntity should return mapped paginated responses")
    void getAuditLogsForEntity_shouldReturnPage() {
        // Arrange
        AuditLog log = AuditLog.builder()
                .id(UUID.randomUUID())
                .entityType("Order")
                .entityId(entityId)
                .action(AuditAction.STATUS_CHANGE)
                .performedBy(testUser)
                .oldValues("{}")
                .newValues("{\"status\":\"CONFIRMED\"}")
                .createdAt(Instant.now())
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        when(auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc("Order", entityId, pageable))
                .thenReturn(new PageImpl<>(List.of(log)));

        // Act
        Page<AuditLogResponse> response = auditService.getAuditLogsForEntity("Order", entityId, pageable);

        // Assert
        assertThat(response).isNotEmpty();
        assertThat(response.getContent().get(0).getEntityType()).isEqualTo("Order");
        assertThat(response.getContent().get(0).getPerformedByName()).isEqualTo("Test User");
    }
}
