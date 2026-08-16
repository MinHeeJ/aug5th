package kr.ac.knue.commonfoundation.batchdefinition;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SaveBatchDefinitionRequest(
    @NotBlank(message = "배치 ID를 입력하세요.")
    @Pattern(regexp = "^[A-Z0-9][A-Z0-9_-]{1,79}$", message = "배치 ID는 영문 대문자, 숫자, _, - 조합 2~80자여야 합니다.")
    String id,
    @NotBlank(message = "실행주기를 입력하세요.")
    @Size(max = 100, message = "실행주기는 100자 이하여야 합니다.")
    String schedule,
    @Size(max = 80, message = "선행 배치 ID는 80자 이하여야 합니다.")
    String predecessorBatchId,
    @Size(max = 4000, message = "파라미터 JSON은 4000자 이하여야 합니다.")
    String parameters,
    @Min(value = 1, message = "최대실행시간은 1초 이상이어야 합니다.")
    @Max(value = 86400, message = "최대실행시간은 86400초 이하여야 합니다.")
    Integer maxRuntimeSeconds,
    @NotBlank(message = "담당자 ID를 입력하세요.")
    @Size(max = 50, message = "담당자 ID는 50자 이하여야 합니다.")
    String ownerId,
    @NotBlank(message = "저장 사유를 입력하세요.")
    @Size(max = 500, message = "저장 사유는 500자 이하여야 합니다.")
    String reason
) {
    public String batchId() {
        return id.trim();
    }

    public String normalizedSchedule() {
        return schedule.trim();
    }

    public String normalizedPredecessorBatchId() {
        return predecessorBatchId == null || predecessorBatchId.isBlank() ? null : predecessorBatchId.trim();
    }

    public String normalizedParameters() {
        return parameters == null || parameters.isBlank() ? "{}" : parameters.trim();
    }

    public int normalizedMaxRuntimeSeconds() {
        return maxRuntimeSeconds == null ? 3600 : maxRuntimeSeconds;
    }

    public String normalizedOwnerId() {
        return ownerId.trim();
    }

    public String normalizedReason() {
        return reason.trim();
    }
}
