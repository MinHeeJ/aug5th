package kr.ac.knue.commonfoundation.batchdefinition;

import java.util.List;

public record BatchDefinitionListResponse(
    List<BatchDefinitionListItem> items,
    int page,
    int size,
    long totalCount,
    String screenId,
    String requiredRole
) {
}
