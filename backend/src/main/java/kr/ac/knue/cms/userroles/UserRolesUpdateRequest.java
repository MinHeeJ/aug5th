package kr.ac.knue.cms.userroles;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record UserRolesUpdateRequest(
    @NotEmpty(message = "하나 이상의 역할을 선택하세요.") List<@Valid UserRoleRequest> roles
) {
}
