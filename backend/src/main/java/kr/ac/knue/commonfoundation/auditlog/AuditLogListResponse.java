package kr.ac.knue.commonfoundation.auditlog;

import java.util.List;

public record AuditLogListResponse(
    List<AuditLogListItem> items,
    int page,
    int size,
    long totalCount,
    String screenId,
    String requiredRole
) {
}
