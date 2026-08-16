package kr.ac.knue.commonfoundation.session;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TerminateSessionRequest(
    @NotBlank(message = "강제종료 사유를 입력하세요.")
    @Size(max = 500, message = "강제종료 사유는 500자 이하여야 합니다.")
    String reason
) {
    public String normalizedReason() {
        return reason.trim();
    }
}
