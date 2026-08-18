package kr.ac.knue.commonfoundation.privacy;

public record SavePrivacyPolicyResponse(
    Long fieldPolicyId,
    String fieldName,
    String privacyGrade,
    Boolean encryptionEnabled,
    Boolean logExcluded,
    String message
) {
}
