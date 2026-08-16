package kr.ac.knue.commonfoundation.userrole;

import java.util.List;

public record UserRoleListResponse(
    List<UserRoleListItem> items,
    int page,
    int size,
    long totalCount,
    String screenId,
    String requiredRole
) {
}
