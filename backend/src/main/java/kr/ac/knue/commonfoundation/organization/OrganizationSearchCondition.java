package kr.ac.knue.commonfoundation.organization;

public record OrganizationSearchCondition(
    int page,
    int size,
    String sort,
    String q,
    String parentOrganizationCode,
    Boolean enabled,
    int offset
) {
    public static OrganizationSearchCondition of(Integer page, Integer size, String sort, String q, String filter) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeSize = size == null ? 20 : size;
        if (safeSize != 20 && safeSize != 50 && safeSize != 100) {
            safeSize = 20;
        }
        FilterParts parts = FilterParts.parse(filter);
        return new OrganizationSearchCondition(
            safePage,
            safeSize,
            sort == null || sort.isBlank() ? "organizationCode" : sort,
            blankToNull(q),
            parts.parentOrganizationCode(),
            parts.enabled(),
            (safePage - 1) * safeSize
        );
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private record FilterParts(String parentOrganizationCode, Boolean enabled) {
        static FilterParts parse(String filter) {
            String parentOrganizationCode = null;
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
                        case "parentOrganizationCode" -> parentOrganizationCode = value;
                        case "enabled" -> enabled = Boolean.valueOf(value);
                        default -> {
                        }
                    }
                }
            }
            return new FilterParts(parentOrganizationCode, enabled);
        }
    }
}
