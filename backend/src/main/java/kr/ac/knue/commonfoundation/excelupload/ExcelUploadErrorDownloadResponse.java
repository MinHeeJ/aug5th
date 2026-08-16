package kr.ac.knue.commonfoundation.excelupload;

public record ExcelUploadErrorDownloadResponse(
    Long uploadId,
    String fileName,
    long errorCount,
    String message,
    String validationRule
) {
}
