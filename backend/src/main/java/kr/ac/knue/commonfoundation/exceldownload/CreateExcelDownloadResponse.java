package kr.ac.knue.commonfoundation.exceldownload;

public record CreateExcelDownloadResponse(
    Long downloadId,
    Long fileId,
    String fileName,
    String status,
    String message
) {
}
