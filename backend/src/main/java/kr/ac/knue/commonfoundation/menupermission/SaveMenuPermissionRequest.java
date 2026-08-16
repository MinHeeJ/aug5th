package kr.ac.knue.commonfoundation.menupermission;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SaveMenuPermissionRequest(
    @NotBlank(message = "메뉴 권한 ID를 입력하세요.") String id,
    @NotNull(message = "접근 허용 여부를 선택하세요.") Boolean allowed,
    @NotBlank(message = "대상 유형을 입력하세요.")
    @Size(max = 20, message = "대상 유형은 20자 이하여야 합니다.")
    String targetType,
    @NotBlank(message = "대상 ID를 입력하세요.")
    @Size(max = 80, message = "대상 ID는 80자 이하여야 합니다.")
    String targetId,
    @NotBlank(message = "변경 사유를 입력하세요.")
    @Size(max = 500, message = "변경 사유는 500자 이하여야 합니다.")
    String reason
) {
    public Long menuPermissionId() {
        try {
            return Long.valueOf(id);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @AssertTrue(message = "메뉴 권한 ID는 숫자여야 합니다.")
    public boolean isNumericIdentifier() {
        return menuPermissionId() != null;
    }

    @AssertTrue(message = "대상 유형은 ROLE, ORG, USER 중 하나여야 합니다.")
    public boolean isKnownTargetType() {
        return "ROLE".equals(targetType) || "ORG".equals(targetType) || "USER".equals(targetType);
    }
}
