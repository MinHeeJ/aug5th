package kr.ac.knue.commonfoundation.batchexecution;

import java.util.List;

public record BatchExecutionListResponse(
    List<BatchExecutionListItem> items,
    int page,
    int size,
    long totalCount,
    String screenId,
    String requiredRole
) {
}
