package kr.ac.knue.commonfoundation.excelupload;

public record ExcelUploadSearchCondition(
    int page,
    int size,
    String sort,
    String q,
    Long templateId,
    String uploadStatus
) {
    public static ExcelUploadSearchCondition of(Integer page, Integer size, String sort, String q, String filter) {
        int normalizedPage = page == null || page < 1 ? 1 : page;
        int normalizedSize = size == null || (size != 20 && size != 50 && size != 100) ? 20 : size;
        return new ExcelUploadSearchCondition(
            normalizedPage,
            normalizedSize,
            sort,
            blankToNull(q),
            parseLongFilter(filter, "templateId"),
            parseFilterValue(filter, "uploadStatus")
        );
    }

    public int offset() {
        return (page - 1) * size;
    }

    private static Long parseLongFilter(String filter, String key) {
        String value = parseFilterValue(filter, key);
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static String parseFilterValue(String filter, String key) {
        if (filter == null || filter.isBlank()) {
            return null;
        }
        for (String part : filter.split(";")) {
            String[] tokens = part.split("=", 2);
            if (tokens.length == 2 && key.equals(tokens[0].trim())) {
                return blankToNull(tokens[1]);
            }
        }
        return null;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
