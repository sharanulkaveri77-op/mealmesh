package com.mealmesh.audit.aspect;

import com.mealmesh.audit.annotation.Auditable;
import com.mealmesh.audit.entity.AuditAction;
import com.mealmesh.audit.service.AuditService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditAspectTest {

    @Mock
    private AuditService auditService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @InjectMocks
    private AuditAspect auditAspect;

    public static class SampleService {
        @Auditable(entityType = "Order", action = AuditAction.STATUS_CHANGE, entityIdParam = "orderId")
        public String updateStatus(UUID orderId, String newStatus, UUID actorId) {
            return "SUCCESS";
        }
    }

    @Test
    @DisplayName("auditMethod should intercept annotated method and call auditService.logAction")
    void auditMethod_shouldInterceptAndLog() throws Throwable {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Method method = SampleService.class.getMethod("updateStatus", UUID.class, String.class, UUID.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{orderId, "DELIVERED", actorId});
        when(methodSignature.getParameterNames()).thenReturn(new String[]{"orderId", "newStatus", "actorId"});
        when(joinPoint.proceed()).thenReturn("SUCCESS");

        // Act
        Object result = auditAspect.auditMethod(joinPoint);

        // Assert
        assertThat(result).isEqualTo("SUCCESS");
        verify(auditService).logAction(
                eq("Order"),
                eq(orderId),
                eq(AuditAction.STATUS_CHANGE),
                isNull(),
                eq("SUCCESS"),
                eq("status"),
                eq(actorId)
        );
    }
}
