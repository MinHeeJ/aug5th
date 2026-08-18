package kr.ac.knue.cms.menus;

import jakarta.validation.constraints.NotNull;

public record MenuStatusRequest(
    @NotNull(message = "사용여부를 입력하세요.") Boolean isUsed,
    String changeReason
) {
}
