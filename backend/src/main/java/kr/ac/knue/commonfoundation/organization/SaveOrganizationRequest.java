package kr.ac.knue.commonfoundation.organization;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record SaveOrganizationRequest(
    @NotBlank(message = "조직코드를 입력하세요.") String id,
    @NotNull(message = "사용여부를 선택하세요.") Boolean enabled,
    LocalDate validTo,
    @NotBlank(message = "변경 사유를 입력하세요.") String reason
) {
    @AssertTrue(message = "종료일은 과거 일자로 지정할 수 없습니다.")
    public boolean isValidToNotBeforeContractBaseline() {
        return validTo == null || !validTo.isBefore(LocalDate.of(2026, 1, 1));
    }
}
