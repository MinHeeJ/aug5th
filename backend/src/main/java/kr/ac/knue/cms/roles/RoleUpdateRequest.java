package kr.ac.knue.cms.roles;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RoleUpdateRequest(
    String roleCode,
    @NotBlank(message = "역할명을 입력하세요.") String roleName,
    @NotBlank(message = "역할 목적을 입력하세요.") String rolePurpose,
    @NotBlank(message = "부여 기준을 입력하세요.") String assignmentCriteria,
    @NotBlank(message = "데이터 범위 기본값을 입력하세요.") String defaultDataScope,
    @NotNull(message = "사용여부를 입력하세요.") Boolean isUsed
) {
}
