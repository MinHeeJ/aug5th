package kr.ac.knue.commonfoundation.codedetail;

import java.util.List;

public record CodeDetailListResponse(
    List<CodeDetailListItem> items,
    int page,
    int size,
    long totalCount,
    String screenId,
    String requiredRole
) {
}
