package kr.ac.knue.commonfoundation.auditlog;

public record AuditLogSearchCondition(
    int page,
    int size,
    String sort,
    String q,
    String logType,
    String result,
    String actorId
) {
    public static AuditLogSearchCondition of(Integer page, Integer size, String sort, String q, String filter) {
        int normalizedPage = page == null || page < 1 ? 1 : page;
        int normalizedSize = size == null || (size != 20 && size != 50 && size != 100) ? 20 : size;
        String normalizedQ = q == null || q.isBlank() ? null : q.trim();
        String logType = null;
        String result = null;
        String actorId = null;
        if (filter != null && !filter.isBlank()) {
            String[] parts = filter.split(";");
            for (String part : parts) {
                String[] pair = part.split("=", 2);
                if (pair.length != 2 || pair[1].isBlank()) {
                    continue;
                }
                switch (pair[0]) {
                    case "logType" -> logType = pair[1].trim();
                    case "result" -> result = pair[1].trim();
                    case "actorId", "actor" -> actorId = pair[1].trim();
                    default -> { }
                }
            }
        }
        return new AuditLogSearchCondition(normalizedPage, normalizedSize, sort, normalizedQ, logType, result, actorId);
    }

    public int offset() {
        return (page - 1) * size;
    }
}
