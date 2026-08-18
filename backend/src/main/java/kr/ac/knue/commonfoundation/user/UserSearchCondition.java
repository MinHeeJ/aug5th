package kr.ac.knue.commonfoundation.user;

public record UserSearchCondition(
    int page,
    int size,
    String sort,
    String q,
    String employeeNo,
    String name,
    String departmentCode,
    String rankName,
    String employmentStatus,
    String roleCode,
    Boolean enabled,
    int offset
) {
    public static UserSearchCondition of(Integer page, Integer size, String sort, String q, String filter) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeSize = size == null ? 20 : size;
        if (safeSize != 20 && safeSize != 50 && safeSize != 100) {
            safeSize = 20;
        }
        FilterParts parts = FilterParts.parse(filter);
        return new UserSearchCondition(
            safePage,
            safeSize,
            sort == null || sort.isBlank() ? "userId" : sort,
            blankToNull(q),
            parts.employeeNo(),
            parts.name(),
            parts.departmentCode(),
            parts.rankName(),
            parts.employmentStatus(),
            parts.roleCode(),
            parts.enabled(),
            (safePage - 1) * safeSize
        );
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private record FilterParts(
        String employeeNo,
        String name,
        String departmentCode,
        String rankName,
        String employmentStatus,
        String roleCode,
        Boolean enabled
    ) {
        static FilterParts parse(String filter) {
            String employeeNo = null;
            String name = null;
            String departmentCode = null;
            String rankName = null;
            String employmentStatus = null;
            String roleCode = null;
            Boolean enabled = null;
            if (filter != null && !filter.isBlank()) {
                String[] tokens = filter.split("[;,]");
                for (String token : tokens) {
                    String[] pair = token.split("=", 2);
                    if (pair.length != 2 || pair[1].isBlank()) {
                        continue;
                    }
                    String key = pair[0].trim();
                    String value = pair[1].trim();
                    switch (key) {
                        case "employeeNo" -> employeeNo = value;
                        case "name" -> name = value;
                        case "departmentCode" -> departmentCode = value;
                        case "rankName" -> rankName = value;
                        case "employmentStatus" -> employmentStatus = value;
                        case "roleCode" -> roleCode = value;
                        case "enabled" -> enabled = Boolean.valueOf(value);
                        default -> {
                        }
                    }
                }
            }
            return new FilterParts(employeeNo, name, departmentCode, rankName, employmentStatus, roleCode, enabled);
        }
    }
}
