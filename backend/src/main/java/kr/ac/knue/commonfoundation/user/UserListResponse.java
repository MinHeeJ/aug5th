package kr.ac.knue.commonfoundation.user;

import java.util.List;

public record UserListResponse(
    List<UserListItem> items,
    int page,
    int size,
    long totalCount,
    String screenId,
    String requiredRole
) {
}
