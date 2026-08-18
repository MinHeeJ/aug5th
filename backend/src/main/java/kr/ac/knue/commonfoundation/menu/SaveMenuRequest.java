package kr.ac.knue.commonfoundation.menu;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SaveMenuRequest(
    @NotBlank(message = "메뉴 ID를 입력하세요.")
    @Size(max = 50, message = "메뉴 ID는 50자 이하여야 합니다.")
    String id,
    @Size(max = 50, message = "상위 메뉴 ID는 50자 이하여야 합니다.")
    String parentMenuId,
    @NotBlank(message = "메뉴명을 입력하세요.")
    @Size(max = 200, message = "메뉴명은 200자 이하여야 합니다.")
    String menuName,
    @NotBlank(message = "화면 ID를 입력하세요.")
    @Size(max = 80, message = "화면 ID는 80자 이하여야 합니다.")
    String screenId,
    @NotBlank(message = "URL을 입력하세요.")
    @Size(max = 300, message = "URL은 300자 이하여야 합니다.")
    String url,
    @NotNull(message = "표시 순서를 입력하세요.")
    @Min(value = 1, message = "표시 순서는 1 이상이어야 합니다.")
    @Max(value = 9999, message = "표시 순서는 9999 이하여야 합니다.")
    Integer displayOrder,
    @NotBlank(message = "변경 사유를 입력하세요.")
    @Size(max = 500, message = "변경 사유는 500자 이하여야 합니다.")
    String reason
) {
    public String menuId() {
        return id;
    }

    @AssertTrue(message = "상위 메뉴는 자기 자신이 될 수 없습니다.")
    public boolean isParentNotSelf() {
        return parentMenuId == null || parentMenuId.isBlank() || id == null || !parentMenuId.equals(id);
    }

    @AssertTrue(message = "메뉴 URL은 / 로 시작해야 합니다.")
    public boolean isUrlPath() {
        return url == null || url.startsWith("/");
    }
}
