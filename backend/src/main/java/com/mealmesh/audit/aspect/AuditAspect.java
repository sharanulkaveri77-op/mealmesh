package com.mealmesh.audit.aspect;

import com.mealmesh.audit.annotation.Auditable;
import com.mealmesh.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditService auditService;

    @Around("@annotation(com.mealmesh.audit.annotation.Auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Auditable auditable = method.getAnnotation(Auditable.class);

        Object[] args = joinPoint.getArgs();
        String[] paramNames = signature.getParameterNames();

        UUID entityId = extractEntityId(args, paramNames, auditable.entityIdParam());
        UUID actorId = extractActorId(args, paramNames);

        Object result = joinPoint.proceed();

        try {
            if (entityId != null) {
                auditService.logAction(
                        auditable.entityType(),
                        entityId,
                        auditable.action(),
                        null,
                        result,
                        "status",
                        actorId
                );
            }
        } catch (Exception e) {
            log.error("Failed to record audit log in aspect: {}", e.getMessage(), e);
        }

        return result;
    }

    private UUID extractEntityId(Object[] args, String[] paramNames, String targetParamName) {
        if (args == null || paramNames == null) return null;
        for (int i = 0; i < paramNames.length; i++) {
            if (paramNames[i].equalsIgnoreCase(targetParamName) && args[i] instanceof UUID uuid) {
                return uuid;
            }
            if (args[i] instanceof UUID uuid && (paramNames[i].toLowerCase().contains("id"))) {
                return uuid;
            }
        }
        return null;
    }

    private UUID extractActorId(Object[] args, String[] paramNames) {
        if (args == null || paramNames == null) return null;
        for (int i = 0; i < paramNames.length; i++) {
            if (paramNames[i].equalsIgnoreCase("actorId") || paramNames[i].equalsIgnoreCase("userId")) {
                if (args[i] instanceof UUID uuid) {
                    return uuid;
                }
            }
        }
        return null;
    }
}
