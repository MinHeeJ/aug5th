package kr.ac.knue.commonfoundation.notice;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public record NoticeSearchCondition(
    int page,
    int size,
    String sort,
    String q,
    String targetRole,
    Boolean important,
    Boolean enabled,
    LocalDate activeOn
) {
    public static NoticeSearchCondition of(Integer page, Integer size, String sort, String q, String filter) {
        int normalizedPage = page == null || page < 1 ? 1 : page;
        int normalizedSize = size == null || (size != 20 && size != 50 && size != 100) ? 20 : size;
        return new NoticeSearchCondition(
            normalizedPage,
            normalizedSize,
            sort,
            blankToNull(q),
            parseFilterValue(filter, "targetRole"),
            parseBooleanFilter(filter, "important"),
            parseBooleanFilter(filter, "enabled"),
            parseDateFilter(filter, "activeOn")
        );
    }

    public int offset() {
        return (page - 1) * size;
    }

    private static LocalDate parseDateFilter(String filter, String key) {
        String value = parseFilterValue(filter, key);
        if (value == null) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private static Boolean parseBooleanFilter(String filter, String key) {
        String value = parseFilterValue(filter, key);
        if (value == null) {
            return null;
        }
        return switch (value.toLowerCase()) {
            case "true", "active", "enabled", "사용", "중요", "활성" -> Boolean.TRUE;
            case "false", "inactive", "disabled", "미사용", "일반", "비활성" -> Boolean.FALSE;
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
