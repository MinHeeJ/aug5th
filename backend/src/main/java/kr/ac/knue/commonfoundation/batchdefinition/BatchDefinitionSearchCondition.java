package kr.ac.knue.commonfoundation.batchdefinition;

public record BatchDefinitionSearchCondition(
    int page,
    int size,
    String sort,
    String q,
    String ownerId,
    String schedule
) {
    public static BatchDefinitionSearchCondition of(Integer page, Integer size, String sort, String q, String filter) {
        int normalizedPage = page == null || page < 1 ? 1 : page;
        int normalizedSize = size == null || (size != 20 && size != 50 && size != 100) ? 20 : size;
        String normalizedQ = q == null || q.isBlank() ? null : q.trim();
        String ownerId = null;
        String schedule = null;
        if (filter != null && !filter.isBlank()) {
            String[] parts = filter.split(";");
            for (String part : parts) {
                String[] pair = part.split("=", 2);
                if (pair.length != 2 || pair[1].isBlank()) {
                    continue;
                }
                switch (pair[0]) {
                    case "ownerId", "owner" -> ownerId = pair[1].trim();
                    case "schedule" -> schedule = pair[1].trim();
                    default -> { }
                }
            }
        }
        return new BatchDefinitionSearchCondition(normalizedPage, normalizedSize, sort, normalizedQ, ownerId, schedule);
    }

    public int offset() {
        return (page - 1) * size;
    }
}
