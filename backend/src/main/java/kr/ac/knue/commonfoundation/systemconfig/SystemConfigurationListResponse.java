package kr.ac.knue.commonfoundation.systemconfig;

import java.util.List;

public record SystemConfigurationListResponse(
    List<SystemConfigurationListItem> items,
    int page,
    int size,
    long totalCount,
    String screenId,
    String requiredRole
) {
}
