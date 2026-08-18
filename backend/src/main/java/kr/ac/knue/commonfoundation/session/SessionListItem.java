package kr.ac.knue.commonfoundation.session;

public record SessionListItem(
    String sessionId,
    String userId,
    String userDisplayName,
    String loginAt,
    String lastActivityAt,
    String ipAddress,
    String sessionStatus,
    String sessionStatusName,
    Long latestTerminationId,
    String latestTerminationType,
    String operationRule
) {
}
