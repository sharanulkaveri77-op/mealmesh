package com.mealmesh.audit.dto;

import com.mealmesh.audit.entity.AuditAction;
import com.mealmesh.audit.entity.AuditLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private UUID id;
    private String entityType;
    private UUID entityId;
    private AuditAction action;
    private UUID performedById;
    private String performedByName;
    private String oldValues;
    private String newValues;
    private String changedFields;
    private String ipAddress;
    private String userAgent;
    private Instant createdAt;

    public static AuditLogResponse fromEntity(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .action(log.getAction())
                .performedById(log.getPerformedBy() != null ? log.getPerformedBy().getId() : null)
                .performedByName(log.getPerformedBy() != null ? log.getPerformedBy().getName() : "SYSTEM")
                .oldValues(log.getOldValues())
                .newValues(log.getNewValues())
                .changedFields(log.getChangedFields())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
