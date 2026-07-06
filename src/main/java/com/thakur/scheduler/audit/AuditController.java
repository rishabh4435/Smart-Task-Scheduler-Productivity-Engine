package com.thakur.scheduler.audit;


import com.thakur.scheduler.task.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;


    @GetMapping("/me")
    public ResponseEntity<Page<AuditLogResponseDto>> getMyAudit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                auditService.getUserAudit(userDetails.getUser().getId(), pageable)
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLogResponseDto>> getAllAudits(
            Pageable pageable
    ) {
        return ResponseEntity.ok(auditService.getAllAudits(pageable));
    }

    @GetMapping("/action/{action}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLogResponseDto>> getByAction(
            @PathVariable AuditAction action,
            Pageable pageable
    ) {
        return ResponseEntity.ok(auditService.getAuditsByAction(action, pageable));
    }

    @GetMapping("/between")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLogResponseDto>> getBetween(
            @RequestParam Instant start,
            @RequestParam Instant end,
            Pageable pageable
    ) {
        return ResponseEntity.ok(auditService.getAuditsBetween(start, end, pageable));
    }

}
