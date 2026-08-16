package kr.ac.knue.commonfoundation.position;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record SavePositionRequest(
    @NotBlank(message = "보직 배정 ID를 입력하세요.") String id,
    @NotNull(message = "활성 여부를 선택하세요.") Boolean active,
    LocalDate validTo,
    @NotBlank(message = "변경 사유를 입력하세요.") String reason
) {
    public Long positionId() {
        try {
            return Long.valueOf(id);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @AssertTrue(message = "보직 배정 ID는 숫자여야 합니다.")
    public boolean isNumericIdentifier() {
        return positionId() != null;
    }

    @AssertTrue(message = "종료일은 과거 일자로 지정할 수 없습니다.")
    public boolean isValidToNotBeforeContractBaseline() {
        return validTo == null || !validTo.isBefore(LocalDate.of(2026, 1, 1));
    }
}
