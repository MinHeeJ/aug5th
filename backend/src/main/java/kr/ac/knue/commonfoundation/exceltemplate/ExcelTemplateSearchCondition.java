package kr.ac.knue.commonfoundation.exceltemplate;

public record ExcelTemplateSearchCondition(
    int page,
    int size,
    String sort,
    String q,
    String businessArea,
    Boolean enabled
) {
    public static ExcelTemplateSearchCondition of(Integer page, Integer size, String sort, String q, String filter) {
        int normalizedPage = page == null || page < 1 ? 1 : page;
        int normalizedSize = size == null || (size != 20 && size != 50 && size != 100) ? 20 : size;
        return new ExcelTemplateSearchCondition(
            normalizedPage,
            normalizedSize,
            sort,
            blankToNull(q),
            parseFilterValue(filter, "businessArea"),
            parseBooleanFilter(filter, "enabled")
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
            case "true", "enabled", "yes", "사용" -> Boolean.TRUE;
            case "false", "disabled", "no", "미사용" -> Boolean.FALSE;
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
