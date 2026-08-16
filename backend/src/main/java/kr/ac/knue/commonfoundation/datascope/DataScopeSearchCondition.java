package kr.ac.knue.commonfoundation.datascope;

public record DataScopeSearchCondition(
    int page,
    int size,
    int offset,
    String sort,
    String q,
    String roleCode,
    String scopeType,
    String organizationCode,
    String businessArea
) {
    public static DataScopeSearchCondition of(Integer page, Integer size, String sort, String q, String filter) {
        int normalizedPage = page == null || page < 1 ? 1 : page;
        int normalizedSize = size == null ? 20 : size;
        if (normalizedSize != 20 && normalizedSize != 50 && normalizedSize != 100) {
            normalizedSize = 20;
        }
        String roleCode = null;
        String scopeType = null;
        String organizationCode = null;
        String businessArea = null;
        if (filter != null && !filter.isBlank()) {
            for (String token : filter.split(";")) {
                String[] pair = token.split("=", 2);
                if (pair.length != 2 || pair[1].isBlank()) {
                    continue;
                }
                switch (pair[0]) {
                    case "roleCode" -> roleCode = pair[1];
                    case "scopeType" -> scopeType = pair[1];
                    case "organizationCode" -> organizationCode = pair[1];
                    case "businessArea" -> businessArea = pair[1];
                    default -> {
                    }
                }
            }
        }
        String normalizedQuery = q == null || q.isBlank() ? null : q.trim();
        return new DataScopeSearchCondition(
            normalizedPage,
            normalizedSize,
            (normalizedPage - 1) * normalizedSize,
            sort,
            normalizedQuery,
            roleCode,
            scopeType,
            organizationCode,
            businessArea
        );
    }
}
