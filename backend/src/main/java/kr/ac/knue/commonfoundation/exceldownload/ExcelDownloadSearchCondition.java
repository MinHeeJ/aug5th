package kr.ac.knue.commonfoundation.exceldownload;

public record ExcelDownloadSearchCondition(
    int page,
    int size,
    String sort,
    String q,
    String requesterId,
    Long fileId
) {
    public static ExcelDownloadSearchCondition of(Integer page, Integer size, String sort, String q, String filter) {
        int normalizedPage = page == null || page < 1 ? 1 : page;
        int normalizedSize = size == null ? 20 : size;
        if (normalizedSize != 20 && normalizedSize != 50 && normalizedSize != 100) {
            normalizedSize = 20;
        }
        String requesterId = null;
        Long fileId = null;
        if (filter != null && !filter.isBlank()) {
            for (String part : filter.split(";")) {
                String[] pair = part.split("=", 2);
                if (pair.length != 2 || pair[1].isBlank()) {
                    continue;
                }
                if ("requesterId".equals(pair[0])) {
                    requesterId = pair[1].trim();
                }
                if ("fileId".equals(pair[0]) && pair[1].trim().matches("^[0-9]+$")) {
                    fileId = Long.valueOf(pair[1].trim());
                }
            }
        }
        return new ExcelDownloadSearchCondition(
            normalizedPage,
            normalizedSize,
            sort,
            q == null || q.isBlank() ? null : q.trim(),
            requesterId,
            fileId
        );
    }

    public int offset() {
        return (page - 1) * size;
    }
}
