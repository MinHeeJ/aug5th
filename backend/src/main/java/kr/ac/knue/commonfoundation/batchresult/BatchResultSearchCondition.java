package kr.ac.knue.commonfoundation.batchresult;

public record BatchResultSearchCondition(
    int page,
    int size,
    String sort,
    String q,
    String batchId,
    String resultStatus,
    Long batchExecutionId
) {
    public static BatchResultSearchCondition of(Integer page, Integer size, String sort, String q, String filter) {
        int normalizedPage = page == null || page < 1 ? 1 : page;
        int normalizedSize = size == null || (size != 20 && size != 50 && size != 100) ? 20 : size;
        String normalizedQ = q == null || q.isBlank() ? null : q.trim();
        String batchId = null;
        String resultStatus = null;
        Long batchExecutionId = null;
        if (filter != null && !filter.isBlank()) {
            for (String part : filter.split(";")) {
                String[] pair = part.split("=", 2);
                if (pair.length != 2 || pair[1].isBlank()) {
                    continue;
                }
                switch (pair[0]) {
                    case "batchId" -> batchId = pair[1].trim();
                    case "status", "resultStatus" -> resultStatus = pair[1].trim();
                    case "batchExecutionId" -> batchExecutionId = parseLong(pair[1].trim());
                    default -> { }
                }
            }
        }
        return new BatchResultSearchCondition(normalizedPage, normalizedSize, sort, normalizedQ, batchId, resultStatus, batchExecutionId);
    }

    public int offset() {
        return (page - 1) * size;
    }

    private static Long parseLong(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
