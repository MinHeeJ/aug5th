package kr.ac.knue.commonfoundation.functionpermission;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SaveFunctionPermissionRequest(
    @NotBlank(message = "기능 권한 ID를 입력하세요.") String id,
    @NotNull(message = "기능 허용 여부를 선택하세요.") Boolean allowed,
    @NotBlank(message = "역할 코드를 입력하세요.")
    @Size(max = 10, message = "역할 코드는 10자 이하여야 합니다.")
    String roleCode,
    @NotBlank(message = "화면 ID를 입력하세요.")
    @Size(max = 80, message = "화면 ID는 80자 이하여야 합니다.")
    String screenId,
    @NotBlank(message = "기능 구분을 입력하세요.")
    @Size(max = 30, message = "기능 구분은 30자 이하여야 합니다.")
    String actionCode,
    @NotBlank(message = "변경 사유를 입력하세요.")
    @Size(max = 500, message = "변경 사유는 500자 이하여야 합니다.")
    String reason
) {
    public Long functionPermissionId() {
        try {
            return Long.valueOf(id);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @AssertTrue(message = "기능 권한 ID는 숫자여야 합니다.")
    public boolean isNumericIdentifier() {
        return functionPermissionId() != null;
    }

    @AssertTrue(message = "기능 구분은 READ, CREATE, UPDATE, DELETE, VERIFY, AUTH, APPROVE, CANCEL_APPROVAL, PRINT, EXCEL, BULK 중 하나여야 합니다.")
    public boolean isKnownActionCode() {
        return "READ".equals(actionCode)
            || "CREATE".equals(actionCode)
            || "UPDATE".equals(actionCode)
            || "DELETE".equals(actionCode)
            || "VERIFY".equals(actionCode)
            || "AUTH".equals(actionCode)
            || "APPROVE".equals(actionCode)
            || "CANCEL_APPROVAL".equals(actionCode)
            || "PRINT".equals(actionCode)
            || "EXCEL".equals(actionCode)
            || "BULK".equals(actionCode)
            || "EXPORT".equals(actionCode);
    }
}
