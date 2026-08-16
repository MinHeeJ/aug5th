package kr.ac.knue.commonfoundation.excelupload;

public record UploadExcelResponse(
    Long uploadId,
    Long templateId,
    String uploadStatus,
    Integer totalCount,
    Integer successCount,
    Integer errorCount,
    Integer excludedCount,
    Integer savedCount,
    String message
) {
}
