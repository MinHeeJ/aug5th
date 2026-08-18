package kr.ac.knue.commonfoundation.codegroup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SaveCodeGroupRequest(
    @NotBlank(message = "코드그룹 ID를 입력하세요.")
    @Size(max = 50, message = "코드그룹 ID는 50자 이하여야 합니다.")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "코드그룹 ID는 영문 대문자, 숫자, _, -만 사용할 수 있습니다.")
    String id,
    @NotBlank(message = "코드그룹명을 입력하세요.")
    @Size(max = 200, message = "코드그룹명은 200자 이하여야 합니다.")
    String groupName,
    @Size(max = 1000, message = "설명은 1000자 이하여야 합니다.")
    String description,
    @Size(max = 100, message = "관리부서는 100자 이하여야 합니다.")
    String managingDepartment,
    @NotNull(message = "사용여부를 선택하세요.")
    Boolean enabled,
    @NotBlank(message = "변경 사유를 입력하세요.")
    @Size(max = 500, message = "변경 사유는 500자 이하여야 합니다.")
    String reason
) {
    public String groupId() {
        return id;
    }
}
