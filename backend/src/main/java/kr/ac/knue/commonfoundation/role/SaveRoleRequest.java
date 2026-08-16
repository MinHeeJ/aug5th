package kr.ac.knue.commonfoundation.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SaveRoleRequest(
    @NotBlank(message = "역할 코드는 필수입니다.")
    @Size(max = 10, message = "역할 코드는 10자 이하여야 합니다.")
    String id,

    @NotNull(message = "사용여부는 필수입니다.")
    Boolean enabled,

    @NotBlank(message = "기본 데이터 범위는 필수입니다.")
    @Size(max = 30, message = "기본 데이터 범위는 30자 이하여야 합니다.")
    String defaultDataScope,

    @Size(max = 2000, message = "목적은 2000자 이하여야 합니다.")
    String purpose,

    @Size(max = 2000, message = "부여 기준은 2000자 이하여야 합니다.")
    String grantCriteria,

    @NotBlank(message = "변경 사유는 필수입니다.")
    @Size(max = 500, message = "변경 사유는 500자 이하여야 합니다.")
    String reason
) {
}
