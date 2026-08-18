package kr.ac.knue.cms.menus;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public record MenuRequest(
    UUID menuId,
    UUID parentMenuId,
    @NotBlank(message = "메뉴 레벨을 입력하세요.") @Pattern(regexp = "MAIN|MIDDLE|SUB", message = "MAIN, MIDDLE, SUB만 허용됩니다.") String menuLevel,
    @NotNull(message = "표시순서를 입력하세요.") @Min(value = 1, message = "표시순서는 1 이상이어야 합니다.") Integer displayOrder,
    @NotBlank(message = "메뉴명을 입력하세요.") String menuName,
    String screenId,
    String url,
    String icon,
    String businessDivision,
    String description,
    @NotNull(message = "사용여부를 입력하세요.") Boolean isUsed,
    String changeReason
) {
}
