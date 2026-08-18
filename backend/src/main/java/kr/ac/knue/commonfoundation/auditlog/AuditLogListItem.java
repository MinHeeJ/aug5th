package kr.ac.knue.commonfoundation.auditlog;

public record AuditLogListItem(
    Long auditLogId,
    String logType,
    String logTypeName,
    String targetKey,
    String actorId,
    String beforeValue,
    String afterValue,
    String result,
    String resultName,
    String operationRule
) {
}
