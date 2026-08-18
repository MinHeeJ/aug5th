package kr.ac.knue.commonfoundation.baseyear;

import java.util.List;

public record BaseYearListResponse(
    List<BaseYearListItem> items,
    int page,
    int size,
    long totalCount,
    String screenId,
    String requiredRole
) {
}
