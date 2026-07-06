package com.thakur.scheduler.audit;

import com.thakur.scheduler.task.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "5. Audit & Logs", description = "Endpoints for tracking user activities, system changes, and compliance logs")
public class AuditController {

    private final AuditService auditService;

    @Operation(
            summary = "Get my audit logs",
            description = "Fetches the paginated audit trail for the currently authenticated user."
    )
    @GetMapping("/me")
    public ResponseEntity<Page<AuditLogResponseDto>> getMyAudit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                auditService.getUserAudit(userDetails.getUser().getId(), pageable)
        );
    }

    @Operation(
            summary = "Get all system audits (Admin Only)",
            description = "Retrieves a paginated list of all system audit logs across all users. STRICTLY requires ADMIN role."
    )
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLogResponseDto>> getAllAudits(
            Pageable pageable
    ) {
        return ResponseEntity.ok(auditService.getAllAudits(pageable));
    }

    @Operation(
            summary = "Filter audits by action (Admin Only)",
            description = "Fetches logs based on specific system actions (e.g., CREATE_TASK, UPDATE_ROLE)."
    )
    @GetMapping("/action/{action}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLogResponseDto>> getByAction(
            @Parameter(description = "The specific action to filter by", example = "CREATE_TASK")
            @PathVariable AuditAction action,
            Pageable pageable
    ) {
        return ResponseEntity.ok(auditService.getAuditsByAction(action, pageable));
    }

    @Operation(
            summary = "Get audits by date range (Admin Only)",
            description = "Fetches audit logs recorded between a specific start and end time."
    )
    @GetMapping("/between")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLogResponseDto>> getBetween(
            @Parameter(description = "Start time in ISO-8601 format", example = "2026-07-01T00:00:00Z")
            @RequestParam Instant start,
            @Parameter(description = "End time in ISO-8601 format", example = "2026-07-31T23:59:59Z")
            @RequestParam Instant end,
            Pageable pageable
    ) {
        return ResponseEntity.ok(auditService.getAuditsBetween(start, end, pageable));
    }
}