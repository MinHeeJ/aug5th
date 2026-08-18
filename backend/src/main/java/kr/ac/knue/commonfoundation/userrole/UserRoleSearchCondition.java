package kr.ac.knue.commonfoundation.userrole;

import java.util.Arrays;

public record UserRoleSearchCondition(
    int page,
    int size,
    String sort,
    String q,
    String roleCode,
    String assignmentSource,
    Boolean active
) {
    public static UserRoleSearchCondition of(Integer page, Integer size, String sort, String q, String filter) {
        int normalizedPage = page == null || page < 1 ? 1 : page;
        int normalizedSize = size == null || !Arrays.asList(20, 50, 100).contains(size) ? 20 : size;
        String trimmedQuery = q == null || q.isBlank() ? null : q.trim();
        String roleCode = null;
        String assignmentSource = null;
        Boolean active = null;
        if (filter != null && !filter.isBlank()) {
            for (String part : filter.split(";")) {
                String[] entry = part.split("=", 2);
                if (entry.length != 2 || entry[1].isBlank()) {
                    continue;
                }
                if ("roleCode".equals(entry[0])) {
                    roleCode = entry[1];
                }
                if ("assignmentSource".equals(entry[0])) {
                    assignmentSource = entry[1];
                }
                if ("active".equals(entry[0])) {
                    active = Boolean.valueOf(entry[1]);
                }
            }
        }
        return new UserRoleSearchCondition(normalizedPage, normalizedSize, sort, trimmedQuery, roleCode, assignmentSource, active);
    }

    public int offset() {
        return (page - 1) * size;
    }
}
