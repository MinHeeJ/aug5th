package kr.ac.knue.commonfoundation.functionpermission;

import java.util.Map;

import kr.ac.knue.commonfoundation.api.ApiException;
import kr.ac.knue.commonfoundation.auth.CurrentUserContext;
import kr.ac.knue.commonfoundation.auth.SessionPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FunctionPermissionManagementService {

    private final FunctionPermissionMapper functionPermissionMapper;
    private final CurrentUserContext currentUserContext;

    public FunctionPermissionManagementService(FunctionPermissionMapper functionPermissionMapper, CurrentUserContext currentUserContext) {
        this.functionPermissionMapper = functionPermissionMapper;
        this.currentUserContext = currentUserContext;
    }

    @Transactional(readOnly = true)
    public FunctionPermissionListResponse listFunctionPermissions(FunctionPermissionSearchCondition condition) {
        return new FunctionPermissionListResponse(
            functionPermissionMapper.selectFunctionPermissions(condition),
            condition.page(),
            condition.size(),
            functionPermissionMapper.countFunctionPermissions(condition),
            "SCR-FUNCTION-PERMISSION",
            "R09"
        );
    }

    @Transactional
    public SaveFunctionPermissionResponse saveFunctionPermission(SaveFunctionPermissionRequest request) {
        SessionPrincipal principal = currentUserContext.current()
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));
        Long functionPermissionId = request.functionPermissionId();
        if (functionPermissionId == null || !functionPermissionMapper.existsFunctionPermission(functionPermissionId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "기능 권한을 찾을 수 없습니다.");
        }
        if (functionPermissionMapper.permissionIdentityMismatch(functionPermissionId, request.roleCode(), request.screenId(), request.actionCode())) {
            throw new ApiException(HttpStatus.CONFLICT, "CONFLICT", "역할·화면·기능구분은 기능 권한 생명주기 식별자와 함께 변경할 수 없습니다.", Map.of("id", "선택한 기능 권한과 역할·화면·기능 정보가 일치하지 않습니다."));
        }
        functionPermissionMapper.updateFunctionPermissionAllowed(functionPermissionId, request.allowed());
        functionPermissionMapper.insertAudit(
            "function_permissions:" + functionPermissionId,
            principal.user().userId(),
            jsonAfterValue(functionPermissionId, request)
        );
        return new SaveFunctionPermissionResponse(
            functionPermissionId,
            request.allowed(),
            request.roleCode(),
            request.screenId(),
            request.actionCode(),
            "기능 권한 관리 저장이 완료되었습니다."
        );
    }

    private static String jsonAfterValue(Long functionPermissionId, SaveFunctionPermissionRequest request) {
        return "{\"functionPermissionId\":" + functionPermissionId
            + ",\"allowed\":" + request.allowed()
            + ",\"roleCode\":\"" + escapeJson(request.roleCode())
            + "\",\"screenId\":\"" + escapeJson(request.screenId())
            + "\",\"actionCode\":\"" + escapeJson(request.actionCode())
            + "\",\"reason\":\"" + escapeJson(request.reason()) + "\"}";
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
