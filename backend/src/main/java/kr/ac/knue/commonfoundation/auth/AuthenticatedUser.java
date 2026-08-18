package kr.ac.knue.commonfoundation.auth;

import java.util.List;

public record AuthenticatedUser(String userId, List<String> roles, String dataScope) {

    public boolean hasRole(String roleCode) {
        return roles.contains(roleCode);
    }
}
