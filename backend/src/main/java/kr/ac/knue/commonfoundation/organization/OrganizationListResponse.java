package kr.ac.knue.commonfoundation.organization;

import java.util.List;

public record OrganizationListResponse(
    List<OrganizationListItem> items,
    int page,
    int size,
    long totalCount,
    String screenId,
    String requiredRole
) {
}
