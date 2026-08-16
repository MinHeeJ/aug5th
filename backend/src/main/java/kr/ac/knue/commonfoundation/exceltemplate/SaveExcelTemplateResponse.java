package kr.ac.knue.commonfoundation.exceltemplate;

public record SaveExcelTemplateResponse(
    Long templateId,
    String businessArea,
    String version,
    boolean enabled,
    int requiredColumnCount,
    String message
) {
}
