package kr.ac.knue.commonfoundation.userrole;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record SaveUserRoleRequest(
    @NotBlank(message = "사용자 역할 ID를 입력하세요.") String id,
    @NotNull(message = "현재 적용 여부를 선택하세요.") Boolean active,
    LocalDate validTo,
    @NotBlank(message = "부여 구분은 필수입니다.")
    @Size(max = 20, message = "부여 구분은 20자 이하여야 합니다.")
    String assignmentSource,
    @NotBlank(message = "변경 사유를 입력하세요.")
    @Size(max = 500, message = "변경 사유는 500자 이하여야 합니다.")
    String reason
) {
    public Long userRoleId() {
        try {
            return Long.valueOf(id);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @AssertTrue(message = "사용자 역할 ID는 숫자여야 합니다.")
    public boolean isNumericIdentifier() {
        return userRoleId() != null;
    }

    @AssertTrue(message = "부여 구분은 MANUAL 또는 POSITION이어야 합니다.")
    public boolean isKnownAssignmentSource() {
        return "MANUAL".equals(assignmentSource) || "POSITION".equals(assignmentSource);
    }

    @AssertTrue(message = "종료일은 2026-01-01 이후여야 합니다.")
    public boolean isValidToAfterBaseline() {
        return validTo == null || !validTo.isBefore(LocalDate.of(2026, 1, 1));
    }
}
