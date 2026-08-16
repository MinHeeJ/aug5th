package kr.ac.knue.commonfoundation.exceltemplate;

import java.util.List;

public record ExcelTemplateListResponse(
    List<ExcelTemplateListItem> items,
    int page,
    int size,
    long totalCount,
    String screenId,
    String requiredRole
) {
}
