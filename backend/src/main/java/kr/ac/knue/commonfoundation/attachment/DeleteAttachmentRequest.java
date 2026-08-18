package kr.ac.knue.commonfoundation.attachment;

import jakarta.validation.constraints.Size;

public record DeleteAttachmentRequest(
    @Size(max = 500, message = "삭제 사유는 500자 이하여야 합니다.")
    String deleteReason,
    @Size(max = 500, message = "처리 사유는 500자 이하여야 합니다.")
    String reason
) {
    public String normalizedDeleteReason() {
        return deleteReason == null || deleteReason.isBlank() ? null : deleteReason.trim();
    }

    public String normalizedReason() {
        return reason == null || reason.isBlank() ? normalizedDeleteReason() : reason.trim();
    }
}
