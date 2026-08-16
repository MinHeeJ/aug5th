package kr.ac.knue.commonfoundation.exceltemplate;

public record ExcelTemplateDownloadResponse(
    Long templateId,
    Long fileId,
    String fileName,
    String message,
    String downloadRule
) {
}
