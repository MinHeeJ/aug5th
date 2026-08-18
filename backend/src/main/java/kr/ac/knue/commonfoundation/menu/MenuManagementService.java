package kr.ac.knue.commonfoundation.menu;

import java.util.Map;

import kr.ac.knue.commonfoundation.api.ApiException;
import kr.ac.knue.commonfoundation.auth.CurrentUserContext;
import kr.ac.knue.commonfoundation.auth.SessionPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MenuManagementService {

    private final MenuMapper menuMapper;
    private final CurrentUserContext currentUserContext;

    public MenuManagementService(MenuMapper menuMapper, CurrentUserContext currentUserContext) {
        this.menuMapper = menuMapper;
        this.currentUserContext = currentUserContext;
    }

    @Transactional(readOnly = true)
    public MenuListResponse listMenus(MenuSearchCondition condition) {
        return new MenuListResponse(
            menuMapper.selectMenus(condition),
            condition.page(),
            condition.size(),
            menuMapper.countMenus(condition),
            "SCR-MENU-MGMT",
            "R09"
        );
    }

    @Transactional
    public SaveMenuResponse saveMenu(SaveMenuRequest request) {
        SessionPrincipal principal = currentUserContext.current()
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));
        String menuId = request.menuId();
        if (!menuMapper.existsMenu(menuId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "메뉴를 찾을 수 없습니다.");
        }
        String parentMenuId = blankToNull(request.parentMenuId());
        if (parentMenuId != null && !menuMapper.parentExists(parentMenuId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "입력값을 확인하세요.", Map.of("parentMenuId", "존재하는 상위 메뉴를 선택하세요."));
        }
        if (menuMapper.screenIdentityMismatch(menuId, request.screenId())) {
            throw new ApiException(HttpStatus.CONFLICT, "CONFLICT", "화면 ID는 메뉴 생명주기 식별자로 변경할 수 없습니다.", Map.of("id", "선택한 메뉴와 화면 정보가 일치하지 않습니다."));
        }
        if (menuMapper.duplicateScreen(menuId, request.screenId())) {
            throw new ApiException(HttpStatus.CONFLICT, "CONFLICT", "동일한 화면 ID의 메뉴가 이미 존재합니다.", Map.of("screenId", "중복 화면 ID입니다."));
        }
        menuMapper.updateMenu(menuId, parentMenuId, request.menuName(), request.url(), request.displayOrder());
        menuMapper.insertAudit(
            "menus:" + menuId,
            principal.user().userId(),
            jsonAfterValue(menuId, parentMenuId, request)
        );
        return new SaveMenuResponse(menuId, request.menuName(), request.screenId(), request.url(), request.displayOrder(), "메뉴 관리 저장이 완료되었습니다.");
    }

    private static String jsonAfterValue(String menuId, String parentMenuId, SaveMenuRequest request) {
        return "{\"menuId\":\"" + escapeJson(menuId)
            + "\",\"parentMenuId\":" + nullableJson(parentMenuId)
            + ",\"menuName\":\"" + escapeJson(request.menuName())
            + "\",\"screenId\":\"" + escapeJson(request.screenId())
            + "\",\"url\":\"" + escapeJson(request.url())
            + "\",\"displayOrder\":" + request.displayOrder()
            + ",\"reason\":\"" + escapeJson(request.reason()) + "\"}";
    }

    private static String nullableJson(String value) {
        String normalized = blankToNull(value);
        return normalized == null ? "null" : "\"" + escapeJson(normalized) + "\"";
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
