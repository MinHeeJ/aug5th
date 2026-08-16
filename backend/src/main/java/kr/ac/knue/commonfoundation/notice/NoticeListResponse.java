package kr.ac.knue.commonfoundation.notice;

import java.util.List;

public record NoticeListResponse(
    List<NoticeListItem> items,
    int page,
    int size,
    long totalCount,
    String screenId,
    String requiredRole
) {
}
