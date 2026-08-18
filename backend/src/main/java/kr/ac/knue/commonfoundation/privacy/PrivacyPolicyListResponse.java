package kr.ac.knue.commonfoundation.privacy;

import java.util.List;

public record PrivacyPolicyListResponse(
    List<PrivacyPolicyListItem> items,
    int page,
    int size,
    long totalCount,
    String screenId,
    String requiredRole
) {
}
