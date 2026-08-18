package kr.ac.knue.commonfoundation.role;

import java.util.List;

public record RoleListResponse(
    List<RoleListItem> items,
    int page,
    int size,
    long totalCount,
    String screenId,
    String requiredRole
) {
}
