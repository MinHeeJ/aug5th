package kr.ac.knue.commonfoundation.session;

public record SaveSessionResponse(
    String sessionId,
    String sessionStatus,
    String terminationType,
    String message
) {
}
