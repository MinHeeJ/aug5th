package kr.ac.knue.cms.permissions;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record MenuPermissionItem(
    UUID permissionId,
    String targetType,
    String targetId,
    @NotNull(message = "메뉴를 선택하세요.") UUID menuId,
    @NotNull(message = "접근 허용 여부를 입력하세요.") Boolean isAllowed
) {
}
