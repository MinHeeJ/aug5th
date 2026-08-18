package kr.ac.knue.commonfoundation.role;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public record RoleSearchCondition(
    int page,
    int size,
    int offset,
    String sort,
    String q,
    Boolean enabled,
    String defaultDataScope
) {
    public static RoleSearchCondition of(Integer page, Integer size, String sort, String q, String filter) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeSize = size == null || !(size == 20 || size == 50 || size == 100) ? 20 : size;
        Map<String, String> filters = parseFilter(filter);
        Boolean enabled = filters.containsKey("enabled") ? Boolean.valueOf(filters.get("enabled")) : null;
        String defaultDataScope = blankToNull(filters.get("defaultDataScope"));
        return new RoleSearchCondition(
            safePage,
            safeSize,
            (safePage - 1) * safeSize,
            blankToNull(sort),
            blankToNull(q),
            enabled,
            defaultDataScope
        );
    }

    private static Map<String, String> parseFilter(String filter) {
        if (filter == null || filter.isBlank()) {
            return Map.of();
        }
        return Arrays.stream(filter.split(";"))
            .map(part -> part.split("=", 2))
            .filter(pair -> pair.length == 2 && !pair[0].isBlank() && !pair[1].isBlank())
            .collect(Collectors.toMap(pair -> pair[0].trim(), pair -> pair[1].trim(), (left, right) -> right));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
