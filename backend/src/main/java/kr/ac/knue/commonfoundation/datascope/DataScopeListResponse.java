package kr.ac.knue.commonfoundation.datascope;

import java.util.List;

public record DataScopeListResponse(
    List<DataScopeListItem> items,
    int page,
    int size,
    long totalCount,
    String screenId,
    String requiredRole
) {
}
