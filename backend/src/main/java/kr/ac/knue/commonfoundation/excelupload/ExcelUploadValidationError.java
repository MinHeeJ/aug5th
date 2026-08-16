package kr.ac.knue.commonfoundation.excelupload;

public record ExcelUploadValidationError(
    int rowNumber,
    String columnName,
    String inputValue,
    String errorCode,
    String errorReason
) {
}
