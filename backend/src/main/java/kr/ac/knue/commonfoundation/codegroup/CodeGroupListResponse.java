package kr.ac.knue.commonfoundation.codegroup;

import java.util.List;

public record CodeGroupListResponse(
    List<CodeGroupListItem> items,
    int page,
    int size,
    long totalCount,
    String screenId,
    String requiredRole
) {
}
