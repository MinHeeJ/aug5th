package kr.ac.knue.commonfoundation.datascope;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SaveDataScopeRequest(
    @NotBlank(message = "데이터 범위 권한 ID를 입력하세요.") String id,
    @NotBlank(message = "역할 코드를 입력하세요.")
    @Size(max = 10, message = "역할 코드는 10자 이하여야 합니다.")
    String roleCode,
    @NotBlank(message = "데이터 범위를 선택하세요.")
    @Size(max = 30, message = "데이터 범위는 30자 이하여야 합니다.")
    String scopeType,
    @Size(max = 50, message = "조직 코드는 50자 이하여야 합니다.")
    String organizationCode,
    @Size(max = 80, message = "업무영역은 80자 이하여야 합니다.")
    String businessArea,
    @NotBlank(message = "변경 사유를 입력하세요.")
    @Size(max = 500, message = "변경 사유는 500자 이하여야 합니다.")
    String reason
) {
    public Long dataScopeId() {
        try {
            return Long.valueOf(id);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @AssertTrue(message = "데이터 범위 권한 ID는 숫자여야 합니다.")
    public boolean isNumericIdentifier() {
        return dataScopeId() != null;
    }

    @AssertTrue(message = "데이터 범위는 SELF, DEPARTMENT, COLLEGE, BUSINESS, ALL 중 하나여야 합니다.")
    public boolean isKnownScopeType() {
        return "SELF".equals(scopeType)
            || "DEPARTMENT".equals(scopeType)
            || "COLLEGE".equals(scopeType)
            || "BUSINESS".equals(scopeType)
            || "ALL".equals(scopeType);
    }

    @AssertTrue(message = "담당업무 범위에는 업무영역을 입력하세요.")
    public boolean isBusinessAreaPresentForBusinessScope() {
        return !"BUSINESS".equals(scopeType) || (businessArea != null && !businessArea.isBlank());
    }

    @AssertTrue(message = "소속학과/단과대학 범위에는 조직 코드를 입력하세요.")
    public boolean isOrganizationPresentForOrganizationScope() {
        return !("DEPARTMENT".equals(scopeType) || "COLLEGE".equals(scopeType)) || (organizationCode != null && !organizationCode.isBlank());
    }
}
