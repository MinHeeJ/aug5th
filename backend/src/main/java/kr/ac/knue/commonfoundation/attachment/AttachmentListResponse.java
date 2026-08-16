package kr.ac.knue.commonfoundation.attachment;

import java.util.List;

public record AttachmentListResponse(
    List<AttachmentListItem> items,
    int page,
    int size,
    long totalCount,
    String screenId,
    String requiredRole
) {
}
