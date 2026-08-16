package kr.ac.knue.commonfoundation.role;

import kr.ac.knue.commonfoundation.api.ApiException;
import kr.ac.knue.commonfoundation.auth.CurrentUserContext;
import kr.ac.knue.commonfoundation.auth.SessionPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleManagementService {

    private final RoleMapper roleMapper;
    private final CurrentUserContext currentUserContext;

    public RoleManagementService(RoleMapper roleMapper, CurrentUserContext currentUserContext) {
        this.roleMapper = roleMapper;
        this.currentUserContext = currentUserContext;
    }

    @Transactional(readOnly = true)
    public RoleListResponse listRoles(RoleSearchCondition condition) {
        return new RoleListResponse(
            roleMapper.selectRoles(condition),
            condition.page(),
            condition.size(),
            roleMapper.countRoles(condition),
            "SCR-ROLE-MGMT",
            "R09"
        );
    }

    @Transactional
    public SaveRoleResponse saveRole(SaveRoleRequest request) {
        SessionPrincipal principal = currentUserContext.current()
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));
        if (!roleMapper.existsRole(request.id())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "역할을 찾을 수 없습니다.");
        }
        roleMapper.updateRoleManagementFields(
            request.id(),
            request.enabled(),
            request.defaultDataScope(),
            request.purpose(),
            request.grantCriteria()
        );
        roleMapper.insertAudit(
            "roles:" + request.id(),
            principal.user().userId(),
            jsonAfterValue(request.id(), request)
        );
        return new SaveRoleResponse(
            request.id(),
            request.enabled(),
            request.defaultDataScope(),
            "역할 관리 저장이 완료되었습니다."
        );
    }

    private static String jsonAfterValue(String roleCode, SaveRoleRequest request) {
        return "{\"roleCode\":\"" + escapeJson(roleCode)
            + "\",\"enabled\":" + request.enabled()
            + ",\"defaultDataScope\":\"" + escapeJson(request.defaultDataScope())
            + "\",\"reason\":\"" + escapeJson(request.reason()) + "\"}";
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
