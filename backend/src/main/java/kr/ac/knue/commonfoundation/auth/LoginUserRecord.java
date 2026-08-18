package kr.ac.knue.commonfoundation.auth;

public record LoginUserRecord(String userId, Boolean enabled, String status, String passwordHash) {
}
