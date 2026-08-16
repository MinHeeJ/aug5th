package kr.ac.knue.commonfoundation.session;

public record SessionSearchCondition(
    int page,
    int size,
    String sort,
    String q,
    String status,
    String ipAddress
) {
    public static SessionSearchCondition of(Integer page, Integer size, String sort, String q, String filter) {
        int normalizedPage = page == null || page < 1 ? 1 : page;
        int normalizedSize = size == null || (size != 20 && size != 50 && size != 100) ? 20 : size;
        String normalizedQ = q == null || q.isBlank() ? null : q.trim();
        String status = null;
        String ipAddress = null;
        if (filter != null && !filter.isBlank()) {
            String[] parts = filter.split(";");
            for (String part : parts) {
                String[] pair = part.split("=", 2);
                if (pair.length != 2 || pair[1].isBlank()) {
                    continue;
                }
                switch (pair[0]) {
                    case "status", "sessionStatus" -> status = pair[1].trim();
                    case "ip", "ipAddress" -> ipAddress = pair[1].trim();
                    default -> { }
                }
            }
        }
        return new SessionSearchCondition(normalizedPage, normalizedSize, sort, normalizedQ, status, ipAddress);
    }

    public int offset() {
        return (page - 1) * size;
    }
}
