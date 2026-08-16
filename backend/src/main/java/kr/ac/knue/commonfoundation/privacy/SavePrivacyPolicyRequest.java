package kr.ac.knue.commonfoundation.privacy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SavePrivacyPolicyRequest(
    @NotBlank(message = "개인정보 정책 ID를 입력하세요.")
    @Pattern(regexp = "^[0-9]+$", message = "개인정보 정책 ID는 숫자여야 합니다.")
    String id,
    @NotBlank(message = "필드명을 입력하세요.")
    @Size(max = 100, message = "필드명은 100자 이하여야 합니다.")
    String fieldName,
    @NotBlank(message = "개인정보 등급을 선택하세요.")
    @Pattern(regexp = "^(PUBLIC|PERSONAL|SENSITIVE)$", message = "개인정보 등급은 PUBLIC, PERSONAL, SENSITIVE 중 하나여야 합니다.")
    String privacyGrade,
    @NotNull(message = "암호화 적용 여부를 선택하세요.")
    Boolean encryptionEnabled,
    @NotBlank(message = "마스킹 규칙을 입력하세요.")
    @Size(max = 100, message = "마스킹 규칙은 100자 이하여야 합니다.")
    String maskingRule,
    @NotNull(message = "로그 제외 여부를 선택하세요.")
    Boolean logExcluded,
    @NotBlank(message = "처리 사유를 입력하세요.")
    @Size(max = 500, message = "처리 사유는 500자 이하여야 합니다.")
    String reason
) {
    public Long fieldPolicyId() {
        return Long.valueOf(id.trim());
    }

    public String normalizedFieldName() {
        return fieldName.trim();
    }

    public String normalizedMaskingRule() {
        return maskingRule.trim();
    }
}
