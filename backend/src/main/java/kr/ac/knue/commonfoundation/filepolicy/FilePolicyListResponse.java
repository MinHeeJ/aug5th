package kr.ac.knue.commonfoundation.filepolicy;

import java.util.List;

public record FilePolicyListResponse(
    List<FilePolicyListItem> items,
    int page,
    int size,
    long totalCount,
    String screenId,
    String requiredRole
) {
}
