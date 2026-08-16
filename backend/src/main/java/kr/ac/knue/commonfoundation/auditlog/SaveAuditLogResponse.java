package kr.ac.knue.commonfoundation.auditlog;

public record SaveAuditLogResponse(
    Long auditLogId,
    String targetKey,
    String result,
    String message
) {
}
