package com.thakur.scheduler.audit;

import java.time.Instant;

public record AuditLogResponseDto(
        String id,
        String userId,
        AuditAction action,
        EntityType entityType,
        String entityId,
        Instant timestamp,
        boolean success,
        String details
) {
}
