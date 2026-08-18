package kr.ac.knue.commonfoundation.position;

public record PositionSearchCondition(
    int page,
    int size,
    String sort,
    String q,
    String positionCode,
    String organizationCode,
    Boolean active,
    int offset
) {
    public static PositionSearchCondition of(Integer page, Integer size, String sort, String q, String filter) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeSize = size == null ? 20 : size;
        if (safeSize != 20 && safeSize != 50 && safeSize != 100) {
            safeSize = 20;
        }
        FilterParts parts = FilterParts.parse(filter);
        return new PositionSearchCondition(
            safePage,
            safeSize,
            sort == null || sort.isBlank() ? "positionCode" : sort,
            blankToNull(q),
            parts.positionCode(),
            parts.organizationCode(),
            parts.active(),
            (safePage - 1) * safeSize
        );
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private record FilterParts(String positionCode, String organizationCode, Boolean active) {
        static FilterParts parse(String filter) {
            String positionCode = null;
            String organizationCode = null;
            Boolean active = null;
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
                        case "positionCode" -> positionCode = value;
                        case "organizationCode" -> organizationCode = value;
                        case "active" -> active = Boolean.valueOf(value);
                        default -> {
                        }
                    }
                }
            }
            return new FilterParts(positionCode, organizationCode, active);
        }
    }
}
