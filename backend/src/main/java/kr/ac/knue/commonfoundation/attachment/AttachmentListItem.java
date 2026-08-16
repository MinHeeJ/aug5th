package kr.ac.knue.commonfoundation.attachment;

import java.time.LocalDateTime;

public record AttachmentListItem(
    Long attachmentId,
    String businessKey,
    String originalName,
    String storedName,
    String extension,
    Long sizeBytes,
    String uploadedBy,
    LocalDateTime uploadedAt,
    String malwareScanResult,
    boolean deleted,
    boolean finalizedRecord,
    boolean storagePresent,
    String integrityStatus,
    String downloadAuthorizationRule,
    String deleteBoundary
) {
}
