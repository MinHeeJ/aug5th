package kr.ac.knue.commonfoundation.excelupload;

public record ExcelUploadTemplate(
    Long templateId,
    String businessArea,
    String version,
    String requiredColumns,
    Boolean enabled
) {
}
