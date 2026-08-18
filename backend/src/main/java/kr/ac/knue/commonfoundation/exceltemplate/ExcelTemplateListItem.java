package kr.ac.knue.commonfoundation.exceltemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExcelTemplateListItem(
    Long templateId,
    String businessArea,
    String businessAreaName,
    String version,
    String requiredColumns,
    Integer requiredColumnCount,
    LocalDate effectiveDate,
    Long downloadFileId,
    String downloadFileName,
    boolean enabled,
    String validationRule,
    String downloadRule,
    LocalDateTime updatedAt
) {
}
