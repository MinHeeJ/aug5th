package kr.ac.knue.commonfoundation.excelupload;

import java.util.List;

public record ExcelUploadListResponse(
    List<ExcelUploadListItem> items,
    int page,
    int size,
    long totalCount,
    String screenId,
    String requiredRole
) {
}
