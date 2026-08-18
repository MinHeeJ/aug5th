package kr.ac.knue.commonfoundation.position;

import java.util.List;

public record PositionListResponse(
    List<PositionListItem> items,
    int page,
    int size,
    long totalCount,
    String screenId,
    String requiredRole
) {
}
