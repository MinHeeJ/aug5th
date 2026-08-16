package kr.ac.knue.commonfoundation.menupermission;

import java.util.List;

public record MenuPermissionListResponse(
    List<MenuPermissionListItem> items,
    int page,
    int size,
    long totalCount,
    String screenId,
    String requiredRole
) {
}
