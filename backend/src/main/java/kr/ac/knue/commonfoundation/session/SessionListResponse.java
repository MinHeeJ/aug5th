package kr.ac.knue.commonfoundation.session;

import java.util.List;

public record SessionListResponse(
    List<SessionListItem> items,
    int page,
    int size,
    long totalCount,
    String screenId,
    String requiredRole
) {
}
