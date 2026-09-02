package com.mealmesh.audit.controller;

import com.mealmesh.audit.dto.AuditLogResponse;
import com.mealmesh.audit.entity.AuditAction;
import com.mealmesh.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<Page<AuditLogResponse>> getAllAuditLogs(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AuditLogResponse> logs = auditService.getAllAuditLogs(pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<Page<AuditLogResponse>> getEntityAuditLogs(
            @PathVariable String entityType,
            @PathVariable UUID entityId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AuditLogResponse> logs = auditService.getAuditLogsForEntity(entityType, entityId, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<AuditLogResponse>> getUserAuditLogs(
            @PathVariable UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AuditLogResponse> logs = auditService.getAuditLogsByUser(userId, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/action/{action}")
    public ResponseEntity<Page<AuditLogResponse>> getActionAuditLogs(
            @PathVariable AuditAction action,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AuditLogResponse> logs = auditService.getAuditLogsByAction(action, pageable);
        return ResponseEntity.ok(logs);
    }
}
