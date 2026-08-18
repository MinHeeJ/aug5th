package kr.ac.knue.commonfoundation.attachment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SaveAttachmentRequest(
    @NotBlank(message = "첨부파일 ID를 입력하세요.")
    @Pattern(regexp = "^[0-9]+$", message = "첨부파일 ID는 숫자여야 합니다.")
    String id,
    @NotBlank(message = "업무자료 키를 입력하세요.")
    @Size(max = 100, message = "업무자료 키는 100자 이하여야 합니다.")
    String businessKey,
    @NotNull(message = "삭제 요청 여부를 선택하세요.")
    Boolean deleteRequested,
    @Size(max = 500, message = "삭제 사유는 500자 이하여야 합니다.")
    String deleteReason,
    @NotBlank(message = "처리 사유를 입력하세요.")
    @Size(max = 500, message = "처리 사유는 500자 이하여야 합니다.")
    String reason
) {
    public Long attachmentId() {
        return Long.valueOf(id.trim());
    }

    public String normalizedBusinessKey() {
        return businessKey.trim();
    }

    public String normalizedDeleteReason() {
        return deleteReason == null || deleteReason.isBlank() ? null : deleteReason.trim();
    }
}
