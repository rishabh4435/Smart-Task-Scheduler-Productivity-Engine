package com.thakur.scheduler.audit;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    private String id;

    private String userId;

    private AuditAction action;

    private EntityType entityType;

    private String entityId;

    private Instant timestamp;

    private String ipAddress;

    private String userAgent;

    private boolean success;

    private String details;
}
