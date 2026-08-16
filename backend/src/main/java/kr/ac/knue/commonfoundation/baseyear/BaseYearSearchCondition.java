package kr.ac.knue.commonfoundation.baseyear;

public record BaseYearSearchCondition(
    int page,
    int size,
    String sort,
    String q,
    Boolean enabled
) {
    public static BaseYearSearchCondition of(Integer page, Integer size, String sort, String q, String filter) {
        int normalizedPage = page == null || page < 1 ? 1 : page;
        int normalizedSize = size == null || (size != 20 && size != 50 && size != 100) ? 20 : size;
        return new BaseYearSearchCondition(
            normalizedPage,
            normalizedSize,
            sort,
            blankToNull(q),
            parseEnabled(filter)
        );
    }

    public int offset() {
        return (page - 1) * size;
    }

    private static Boolean parseEnabled(String filter) {
        String value = parseFilterValue(filter, "enabled");
        if (value == null) {
            return null;
        }
        return switch (value.toLowerCase()) {
            case "true", "active", "enabled", "사용", "활성" -> Boolean.TRUE;
            case "false", "inactive", "disabled", "미사용", "비활성" -> Boolean.FALSE;
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
