package kr.ac.knue.commonfoundation.excelupload;

import java.time.LocalDateTime;

public record ExcelUploadListItem(
    Long uploadId,
    Long templateId,
    String businessArea,
    String businessAreaName,
    String version,
    String uploaderId,
    String fileName,
    Integer totalCount,
    Integer successCount,
    Integer errorCount,
    Integer excludedCount,
    Integer savedCount,
    Integer processingTimeMs,
    String uploadStatus,
    LocalDateTime uploadedAt,
    String transactionRule,
    String validationRule
) {
}
