package kr.ac.knue.commonfoundation.menupermission;

import java.util.Map;

import kr.ac.knue.commonfoundation.api.ApiException;
import kr.ac.knue.commonfoundation.auth.CurrentUserContext;
import kr.ac.knue.commonfoundation.auth.SessionPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MenuPermissionManagementService {

    private final MenuPermissionMapper menuPermissionMapper;
    private final CurrentUserContext currentUserContext;

    public MenuPermissionManagementService(MenuPermissionMapper menuPermissionMapper, CurrentUserContext currentUserContext) {
        this.menuPermissionMapper = menuPermissionMapper;
        this.currentUserContext = currentUserContext;
    }

    @Transactional(readOnly = true)
    public MenuPermissionListResponse listMenuPermissions(MenuPermissionSearchCondition condition) {
        return new MenuPermissionListResponse(
            menuPermissionMapper.selectMenuPermissions(condition),
            condition.page(),
            condition.size(),
            menuPermissionMapper.countMenuPermissions(condition),
            "SCR-MENU-PERMISSION",
            "R09"
        );
    }

    @Transactional
    public SaveMenuPermissionResponse saveMenuPermission(SaveMenuPermissionRequest request) {
        SessionPrincipal principal = currentUserContext.current()
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));
        Long menuPermissionId = request.menuPermissionId();
        if (menuPermissionId == null || !menuPermissionMapper.existsMenuPermission(menuPermissionId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "메뉴 권한을 찾을 수 없습니다.");
        }
        if (menuPermissionMapper.permissionIdentityMismatch(menuPermissionId, request.targetType(), request.targetId())) {
            throw new ApiException(HttpStatus.CONFLICT, "CONFLICT", "대상 유형과 대상 ID는 메뉴 권한 생명주기 식별자와 함께 변경할 수 없습니다.", Map.of("id", "선택한 메뉴 권한과 대상 정보가 일치하지 않습니다."));
        }
        menuPermissionMapper.updateMenuPermissionAllowed(menuPermissionId, request.allowed());
        menuPermissionMapper.insertAudit(
            "menu_permissions:" + menuPermissionId,
            principal.user().userId(),
            jsonAfterValue(menuPermissionId, request)
        );
        return new SaveMenuPermissionResponse(
            menuPermissionId,
            request.allowed(),
            request.targetType(),
            request.targetId(),
            "메뉴 권한 관리 저장이 완료되었습니다."
        );
    }

    private static String jsonAfterValue(Long menuPermissionId, SaveMenuPermissionRequest request) {
        return "{\"menuPermissionId\":" + menuPermissionId
            + ",\"allowed\":" + request.allowed()
            + ",\"targetType\":\"" + escapeJson(request.targetType())
            + "\",\"targetId\":\"" + escapeJson(request.targetId())
            + "\",\"reason\":\"" + escapeJson(request.reason()) + "\"}";
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
