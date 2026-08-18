package kr.ac.knue.commonfoundation.batchresult;

import java.util.List;

public record BatchResultListResponse(
    List<BatchResultListItem> items,
    int page,
    int size,
    long totalCount,
    String screenId,
    String requiredRole
) {
}
