package com.mealmesh.audit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mealmesh.audit.dto.AuditLogResponse;
import com.mealmesh.audit.entity.AuditAction;
import com.mealmesh.audit.entity.AuditLog;
import com.mealmesh.audit.repository.AuditLogRepository;
import com.mealmesh.auth.security.UserPrincipal;
import com.mealmesh.user.entity.User;
import com.mealmesh.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * Persist an audit log entry.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog logAction(String entityType, UUID entityId, AuditAction action,
                              Object oldValues, Object newValues, String changedFields, UUID actorId) {
        String oldJson = serializeObject(oldValues);
        String newJson = serializeObject(newValues);

        User performedBy = null;
        if (actorId != null) {
            performedBy = userRepository.findById(actorId).orElse(null);
        } else {
            performedBy = resolveCurrentUser();
        }

        String ipAddress = resolveClientIp();
        String userAgent = resolveUserAgent();

        AuditLog auditLog = AuditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .performedBy(performedBy)
                .oldValues(oldJson)
                .newValues(newJson)
                .changedFields(changedFields)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(Instant.now())
                .build();

        AuditLog saved = auditLogRepository.save(auditLog);
        log.info("Audit log recorded: id={}, entityType={}, entityId={}, action={}",
                saved.getId(), entityType, entityId, action);
        return saved;
    }

    /**
     * Overload that auto-detects the actor from SecurityContext.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog logAction(String entityType, UUID entityId, AuditAction action,
                              Object oldValues, Object newValues, String changedFields) {
        return logAction(entityType, entityId, action, oldValues, newValues, changedFields, null);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogsForEntity(String entityType, UUID entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId, pageable)
                .map(AuditLogResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAuditLogsForEntity(String entityType, UUID entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId)
                .stream()
                .map(AuditLogResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogsByUser(UUID userId, Pageable pageable) {
        return auditLogRepository.findByPerformedById(userId, pageable)
                .map(AuditLogResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogsByAction(AuditAction action, Pageable pageable) {
        return auditLogRepository.findByActionOrderByCreatedAtDesc(action, pageable)
                .map(AuditLogResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAllAuditLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(AuditLogResponse::fromEntity);
    }

    private User resolveCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
                return userRepository.findById(principal.getId()).orElse(null);
            }
        } catch (Exception e) {
            log.debug("No authenticated user in context for audit logging: {}", e.getMessage());
        }
        return null;
    }

    private String resolveClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isBlank()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.debug("Could not resolve client IP: {}", e.getMessage());
        }
        return null;
    }

    private String resolveUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                return attributes.getRequest().getHeader("User-Agent");
            }
        } catch (Exception e) {
            log.debug("Could not resolve User-Agent: {}", e.getMessage());
        }
        return null;
    }

    private String serializeObject(Object obj) {
        if (obj == null) return null;
        if (obj instanceof String str) return str;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Failed to serialize audit object: {}", e.getMessage());
            return String.valueOf(obj);
        }
    }
}
