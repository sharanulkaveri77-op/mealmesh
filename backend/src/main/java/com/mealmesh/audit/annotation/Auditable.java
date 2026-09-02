package com.mealmesh.audit.annotation;

import com.mealmesh.audit.entity.AuditAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to declaratively mark methods for audit logging.
 * Spring AOP will intercept execution and persist audit trails.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    String entityType();
    AuditAction action();
    String entityIdParam() default "orderId";
}
