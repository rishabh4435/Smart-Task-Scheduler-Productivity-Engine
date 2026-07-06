package com.thakur.scheduler.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface AuditService {
    void log(String userId,
             AuditAction action,
             EntityType entityType,
             String entityId,
             boolean success,
             String details
    );



    Page<AuditLogResponseDto> getAllAudits(Pageable pageable);

    // Make sure your interface signatures match the new Page return types
    Page<AuditLogResponseDto> getUserAudit(String userId, Pageable pageable);
    Page<AuditLogResponseDto> getAuditsByAction(AuditAction action, Pageable pageable);
    Page<AuditLogResponseDto> getAuditsBetween(Instant start, Instant end, Pageable pageable);
}
