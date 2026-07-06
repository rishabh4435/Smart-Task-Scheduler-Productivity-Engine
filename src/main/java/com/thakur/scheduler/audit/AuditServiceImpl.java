package com.thakur.scheduler.audit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void log(
            String userId,
            AuditAction action,
            EntityType entityType,
            String entityId,
            boolean success,
            String details
    ) {

        String ipAddress = "UNKNOWN";
        String userAgent = "UNKNOWN";
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            ipAddress = request.getRemoteAddr();

            String forwardedFor = request.getHeader("X-Forwarded-For");
            if (forwardedFor != null && !forwardedFor.isEmpty()) {
                ipAddress = forwardedFor.split(",")[0];
            }

            userAgent = request.getHeader("User-Agent");
        }
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .timestamp(Instant.now())
                .success(success)
                .details(details)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();


        auditLogRepository.save(auditLog);
    }



    @Override
    public Page<AuditLogResponseDto> getAllAudits(Pageable pageable) {
        return auditLogRepository.findAll(pageable)
                .map(AuditLogMapper::toResponseDto);
    }

    @Override
    public Page<AuditLogResponseDto> getUserAudit(String userId, Pageable pageable) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable)
                .map(AuditLogMapper::toResponseDto);
    }

    @Override
    public Page<AuditLogResponseDto> getAuditsByAction(AuditAction action, Pageable pageable) {
        return auditLogRepository.findByAction(action, pageable)
                .map(AuditLogMapper::toResponseDto);
    }

    @Override
    public Page<AuditLogResponseDto> getAuditsBetween(Instant start, Instant end, Pageable pageable) {
        return auditLogRepository.findByTimestampBetween(start, end, pageable)
                .map(AuditLogMapper::toResponseDto);
    }


}
