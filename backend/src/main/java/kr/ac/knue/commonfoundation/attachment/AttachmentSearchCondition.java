package kr.ac.knue.commonfoundation.attachment;

public record AttachmentSearchCondition(
    int page,
    int size,
    String sort,
    String q,
    String businessKey,
    String malwareScanResult,
    Boolean deleted,
    String integrityStatus
) {
    public static AttachmentSearchCondition of(Integer page, Integer size, String sort, String q, String filter) {
        int normalizedPage = page == null || page < 1 ? 1 : page;
        int normalizedSize = size == null || (size != 20 && size != 50 && size != 100) ? 20 : size;
        return new AttachmentSearchCondition(
            normalizedPage,
            normalizedSize,
            sort,
            blankToNull(q),
            parseFilterValue(filter, "businessKey"),
            parseFilterValue(filter, "malwareScanResult"),
            parseBooleanFilter(filter, "deleted"),
            parseFilterValue(filter, "integrityStatus")
        );
    }

    public int offset() {
        return (page - 1) * size;
    }

    private static Boolean parseBooleanFilter(String filter, String key) {
        String value = parseFilterValue(filter, key);
        if (value == null) {
            return null;
        }
        return switch (value.toLowerCase()) {
            case "true", "deleted", "yes", "삭제", "논리삭제" -> Boolean.TRUE;
            case "false", "active", "no", "정상", "미삭제" -> Boolean.FALSE;
            default -> null;
        };
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
