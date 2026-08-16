package kr.ac.knue.commonfoundation.codedetail;

public record CodeDetailSearchCondition(
    int page,
    int size,
    String sort,
    String q,
    String groupId,
    Boolean active
) {
    public static CodeDetailSearchCondition of(Integer page, Integer size, String sort, String q, String filter) {
        int normalizedPage = page == null || page < 1 ? 1 : page;
        int normalizedSize = size == null || (size != 20 && size != 50 && size != 100) ? 20 : size;
        return new CodeDetailSearchCondition(
            normalizedPage,
            normalizedSize,
            sort,
            blankToNull(q),
            parseFilterValue(filter, "groupId"),
            parseActive(filter)
        );
    }

    public int offset() {
        return (page - 1) * size;
    }

    private static Boolean parseActive(String filter) {
        String value = parseFilterValue(filter, "active");
        if (value == null) {
            return null;
        }
        return switch (value.toLowerCase()) {
            case "true", "active", "사용", "활성" -> Boolean.TRUE;
            case "false", "inactive", "미사용", "비활성" -> Boolean.FALSE;
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
