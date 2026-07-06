package com.thakur.scheduler.audit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock private AuditLogRepository auditLogRepository;
    @InjectMocks private AuditServiceImpl auditService;

    @Test
    void testLogCreation() {
        // Without real HTTP context, ipAddress and userAgent should default to "UNKNOWN"
        auditService.log("user123", AuditAction.CREATE_TASK, EntityType.TASK, "task1", true, "Test Details");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());

        AuditLog savedLog = captor.getValue();
        assertEquals("user123", savedLog.getUserId());
        assertEquals(AuditAction.CREATE_TASK, savedLog.getAction());
        assertEquals("UNKNOWN", savedLog.getIpAddress());
        assertTrue(savedLog.isSuccess());
    }

    @Test
    void testGetAllAudits_Pagination() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        AuditLog log1 = new AuditLog();
        log1.setId("1");
        log1.setTimestamp(Instant.now());

        Page<AuditLog> pagedResult = new PageImpl<>(List.of(log1), pageRequest, 1);

        when(auditLogRepository.findAll(pageRequest)).thenReturn(pagedResult);

        Page<AuditLogResponseDto> response = auditService.getAllAudits(pageRequest);

        assertEquals(1, response.getTotalElements());
        assertEquals("1", response.getContent().getFirst().id());
    }
}