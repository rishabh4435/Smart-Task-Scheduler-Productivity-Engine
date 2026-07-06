package com.thakur.scheduler.audit;

public final class AuditLogMapper {

    public static AuditLogResponseDto toResponseDto(AuditLog auditLog) {
        return new AuditLogResponseDto(
                auditLog.getId(),
                auditLog.getUserId(),
                auditLog.getAction(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                auditLog.getTimestamp(),
                auditLog.isSuccess(),
                auditLog.getDetails()
        );
    }

    private AuditLogMapper() {}
}
