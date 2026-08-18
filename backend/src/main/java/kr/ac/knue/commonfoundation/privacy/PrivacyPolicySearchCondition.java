package kr.ac.knue.commonfoundation.privacy;

public record PrivacyPolicySearchCondition(
    int page,
    int size,
    String sort,
    String q,
    String privacyGrade,
    Boolean encryptionEnabled,
    Boolean logExcluded
) {
    public static PrivacyPolicySearchCondition of(Integer page, Integer size, String sort, String q, String filter) {
        int normalizedPage = page == null || page < 1 ? 1 : page;
        int normalizedSize = size == null || (size != 20 && size != 50 && size != 100) ? 20 : size;
        String normalizedQ = q == null || q.isBlank() ? null : q.trim();
        String grade = null;
        Boolean encryption = null;
        Boolean logExcluded = null;
        if (filter != null && !filter.isBlank()) {
            String[] parts = filter.split(";");
            for (String part : parts) {
                String[] pair = part.split("=", 2);
                if (pair.length != 2 || pair[1].isBlank()) {
                    continue;
                }
                switch (pair[0]) {
                    case "privacyGrade" -> grade = pair[1].trim();
                    case "encryptionEnabled" -> encryption = Boolean.valueOf(pair[1].trim());
                    case "logExcluded" -> logExcluded = Boolean.valueOf(pair[1].trim());
                    default -> { }
                }
            }
        }
        return new PrivacyPolicySearchCondition(normalizedPage, normalizedSize, sort, normalizedQ, grade, encryption, logExcluded);
    }

    public int offset() {
        return (page - 1) * size;
    }
}
