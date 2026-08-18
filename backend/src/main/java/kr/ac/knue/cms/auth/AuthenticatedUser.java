package kr.ac.knue.cms.auth;

import java.util.List;
import java.util.UUID;

public record AuthenticatedUser(UUID userId, String loginId, String staffName, List<String> roleCodes) {
    public boolean hasRole(String roleCode) {
        return roleCodes != null && roleCodes.contains(roleCode);
    }
}
