package kr.ac.knue.commonfoundation.auth;

public record SessionPrincipal(String sessionId, AuthenticatedUser user) {
}
