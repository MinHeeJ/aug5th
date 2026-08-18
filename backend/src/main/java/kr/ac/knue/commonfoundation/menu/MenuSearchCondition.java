package kr.ac.knue.commonfoundation.menu;

public record MenuSearchCondition(
    int page,
    int size,
    int offset,
    String sort,
    String q,
    String parentMenuId,
    String screenId
) {
    public static MenuSearchCondition of(Integer page, Integer size, String sort, String q, String filter) {
        int normalizedPage = page == null || page < 1 ? 1 : page;
        int normalizedSize = size == null ? 20 : size;
        if (normalizedSize != 20 && normalizedSize != 50 && normalizedSize != 100) {
            normalizedSize = 20;
        }
        String parentMenuId = null;
        String screenId = null;
        if (filter != null && !filter.isBlank()) {
            for (String token : filter.split(";")) {
                String[] pair = token.split("=", 2);
                if (pair.length != 2 || pair[1].isBlank()) {
                    continue;
                }
                switch (pair[0]) {
                    case "parentMenuId" -> parentMenuId = pair[1];
                    case "screenId" -> screenId = pair[1];
                    default -> {
                    }
                }
            }
        }
        String normalizedQuery = q == null || q.isBlank() ? null : q.trim();
        return new MenuSearchCondition(normalizedPage, normalizedSize, (normalizedPage - 1) * normalizedSize, sort, normalizedQuery, parentMenuId, screenId);
    }
}
