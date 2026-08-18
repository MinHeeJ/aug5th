package kr.ac.knue.commonfoundation.auditlog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SaveAuditLogRequest(
    @NotBlank(message = "감사 로그 ID를 입력하세요.")
    @Pattern(regexp = "^[0-9]+$", message = "감사 로그 ID는 숫자여야 합니다.")
    String id,
    @NotBlank(message = "확인 사유를 입력하세요.")
    @Size(max = 500, message = "확인 사유는 500자 이하여야 합니다.")
    String reason
) {
    public long auditLogId() {
        return Long.parseLong(id.trim());
    }

    public String normalizedReason() {
        return reason.trim();
    }
}
