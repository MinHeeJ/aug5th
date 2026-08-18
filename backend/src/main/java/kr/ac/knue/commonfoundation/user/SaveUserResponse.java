package kr.ac.knue.commonfoundation.user;

public record SaveUserResponse(String userId, Boolean enabled, String status, String message) {
}
