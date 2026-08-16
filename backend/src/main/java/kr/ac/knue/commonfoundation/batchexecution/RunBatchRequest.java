package kr.ac.knue.commonfoundation.batchexecution;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RunBatchRequest(
    @NotBlank(message = "배치 ID를 입력하세요.")
    @Size(max = 80, message = "배치 ID는 80자 이하여야 합니다.")
    String id,
    @Size(max = 2000, message = "파라미터는 2000자 이하여야 합니다.")
    String parameters,
    @NotBlank(message = "실행 사유를 입력하세요.")
    @Size(max = 500, message = "실행 사유는 500자 이하여야 합니다.")
    String reason
) {
    public String batchId() {
        return id.trim();
    }

    public String normalizedParameters() {
        return parameters == null || parameters.isBlank() ? "{}" : parameters.trim();
    }

    public String normalizedReason() {
        return reason.trim();
    }
}
