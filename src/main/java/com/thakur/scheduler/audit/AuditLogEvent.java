package com.thakur.scheduler.audit;



public record AuditLogEvent(
        String userId,
        AuditAction action,
        EntityType entityType,
        String entityId,
        boolean success,
        String details
) {
}