package kr.ac.knue.cms.permissions;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record MenuPermissionUpdateRequest(
    @NotEmpty(message = "하나 이상의 메뉴 권한을 선택하세요.") List<@Valid MenuPermissionItem> permissions,
    String changeReason
) {
}
