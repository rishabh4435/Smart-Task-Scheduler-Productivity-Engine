package com.thakur.scheduler.audit;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditEventListener {

    private final AuditService auditService;


    @Async
    @EventListener
    public void handleAuditLogEvent(AuditLogEvent event) {
        log.debug("Catching audit event for action: {}", event.action());

        auditService.log(
                event.userId(),
                event.action(),
                event.entityType(),
                event.entityId(),
                event.success(),
                event.details()
        );
    }
}