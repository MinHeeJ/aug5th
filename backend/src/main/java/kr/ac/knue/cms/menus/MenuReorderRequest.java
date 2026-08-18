package kr.ac.knue.cms.menus;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record MenuReorderRequest(
    UUID parentMenuId,
    @NotEmpty(message = "재정렬할 메뉴를 선택하세요.") List<UUID> orderedMenuIds
) {
}
