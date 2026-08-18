package kr.ac.knue.commonfoundation.privacy;

public record PrivacyPolicyListItem(
    Long fieldPolicyId,
    String fieldName,
    String privacyGrade,
    String privacyGradeName,
    Boolean encryptionEnabled,
    String maskingRule,
    Boolean logExcluded,
    String policyRule,
    String auditRule
) {
}
