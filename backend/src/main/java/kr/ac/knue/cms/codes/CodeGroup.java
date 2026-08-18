package kr.ac.knue.cms.codes;

import jakarta.validation.constraints.NotBlank;

public record CodeGroup(
    @NotBlank(message = "그룹ID는 필수입니다.") String groupId,
    @NotBlank(message = "그룹명은 필수입니다.") String groupName,
    String description,
    String managingDepartment,
    Boolean isUsed
) {
}
