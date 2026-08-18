package kr.ac.knue.commonfoundation.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SaveUserRequest(
    @NotBlank(message = "사용자 ID를 입력하세요.") String id,
    @NotNull(message = "사용여부를 선택하세요.") Boolean enabled,
    @NotBlank(message = "상태를 선택하세요.") @Pattern(regexp = "ACTIVE|INACTIVE|LOCKED", message = "허용되지 않은 상태입니다.") String status,
    String roleSummary,
    @NotBlank(message = "변경 사유를 입력하세요.") String reason
) {
}
