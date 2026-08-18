package kr.ac.knue.cms.auth;

public interface AuthenticationPort {
    AuthenticatedUser authenticate(String username, String password);
}
