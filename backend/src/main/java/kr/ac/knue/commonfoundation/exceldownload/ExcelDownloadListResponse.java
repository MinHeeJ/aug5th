package kr.ac.knue.commonfoundation.exceldownload;

import java.util.List;

public record ExcelDownloadListResponse(
    List<ExcelDownloadListItem> items,
    int page,
    int size,
    long totalCount,
    String screenId,
    String requiredRole
) {
}
