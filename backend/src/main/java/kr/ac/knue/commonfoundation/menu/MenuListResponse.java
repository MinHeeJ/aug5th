package kr.ac.knue.commonfoundation.menu;

import java.util.List;

public record MenuListResponse(
    List<MenuListItem> items,
    int page,
    int size,
    long totalCount,
    String screenId,
    String requiredRole
) {
}
