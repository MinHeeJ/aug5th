package kr.ac.knue.commonfoundation.functionpermission;

import java.util.List;

public record FunctionPermissionListResponse(
    List<FunctionPermissionListItem> items,
    int page,
    int size,
    long totalCount,
    String screenId,
    String requiredRole
) {
}
