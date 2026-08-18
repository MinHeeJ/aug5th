package kr.ac.knue.commonfoundation.auditlog;

import jakarta.validation.Valid;
import kr.ac.knue.commonfoundation.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class AuditLogManagementController {

    private final AuditLogManagementService auditLogManagementService;

    public AuditLogManagementController(AuditLogManagementService auditLogManagementService) {
        this.auditLogManagementService = auditLogManagementService;
    }

    @GetMapping("/api/admin/audit-logs")
    public ApiResponse<AuditLogListResponse> listAuditLogs(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String filter
    ) {
        return ApiResponse.ok(auditLogManagementService.listAuditLogs(AuditLogSearchCondition.of(page, size, sort, q, filter)));
    }

    @PostMapping("/api/admin/audit-logs")
    public ApiResponse<SaveAuditLogResponse> saveAuditLog(@Valid @RequestBody SaveAuditLogRequest request) {
        return ApiResponse.ok(auditLogManagementService.saveAuditLog(request));
    }
}
