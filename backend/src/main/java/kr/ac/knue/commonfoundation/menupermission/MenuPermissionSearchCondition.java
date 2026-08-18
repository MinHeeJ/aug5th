package kr.ac.knue.commonfoundation.menupermission;

import java.util.Arrays;

public record MenuPermissionSearchCondition(
    int page,
    int size,
    String sort,
    String q,
    String targetType,
    String targetId,
    Boolean allowed
) {
    public static MenuPermissionSearchCondition of(Integer page, Integer size, String sort, String q, String filter) {
        int normalizedPage = page == null || page < 1 ? 1 : page;
        int normalizedSize = size == null || !Arrays.asList(20, 50, 100).contains(size) ? 20 : size;
        String trimmedQuery = q == null || q.isBlank() ? null : q.trim();
        String targetType = null;
        String targetId = null;
        Boolean allowed = null;
        if (filter != null && !filter.isBlank()) {
            for (String part : filter.split(";")) {
                String[] entry = part.split("=", 2);
                if (entry.length != 2 || entry[1].isBlank()) {
                    continue;
                }
                if ("targetType".equals(entry[0])) {
                    targetType = entry[1];
                }
                if ("targetId".equals(entry[0])) {
                    targetId = entry[1];
                }
                if ("allowed".equals(entry[0])) {
                    allowed = Boolean.valueOf(entry[1]);
                }
            }
        }
        return new MenuPermissionSearchCondition(normalizedPage, normalizedSize, sort, trimmedQuery, targetType, targetId, allowed);
    }

    public int offset() {
        return (page - 1) * size;
    }
}
