package kr.ac.knue.commonfoundation.batchexecution;

public record BatchExecutionSearchCondition(
    int page,
    int size,
    String sort,
    String q,
    String batchId,
    String status,
    String requestedBy
) {
    public static BatchExecutionSearchCondition of(Integer page, Integer size, String sort, String q, String filter) {
        int normalizedPage = page == null || page < 1 ? 1 : page;
        int normalizedSize = size == null || (size != 20 && size != 50 && size != 100) ? 20 : size;
        String normalizedQ = q == null || q.isBlank() ? null : q.trim();
        String batchId = null;
        String status = null;
        String requestedBy = null;
        if (filter != null && !filter.isBlank()) {
            for (String part : filter.split(";")) {
                String[] pair = part.split("=", 2);
                if (pair.length != 2 || pair[1].isBlank()) {
                    continue;
                }
                switch (pair[0]) {
                    case "batchId" -> batchId = pair[1].trim();
                    case "status", "executionStatus" -> status = pair[1].trim();
                    case "requestedBy" -> requestedBy = pair[1].trim();
                    default -> { }
                }
            }
        }
        return new BatchExecutionSearchCondition(normalizedPage, normalizedSize, sort, normalizedQ, batchId, status, requestedBy);
    }

    public int offset() {
        return (page - 1) * size;
    }
}
