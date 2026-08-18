package kr.ac.knue.commonfoundation.exceldownload;

import java.time.LocalDateTime;

public record ExcelDownloadListItem(
    Long downloadId,
    String requesterId,
    String queryCondition,
    String dataScopeApplied,
    Long fileId,
    String fileName,
    String extension,
    Long sizeBytes,
    LocalDateTime createdAt,
    String generationRule,
    String boundaryRule
) {
}
