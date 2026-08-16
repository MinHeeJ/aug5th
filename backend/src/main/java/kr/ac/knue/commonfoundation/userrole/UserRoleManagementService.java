package kr.ac.knue.commonfoundation.userrole;

import java.time.LocalDate;

import kr.ac.knue.commonfoundation.api.ApiException;
import kr.ac.knue.commonfoundation.auth.CurrentUserContext;
import kr.ac.knue.commonfoundation.auth.SessionPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserRoleManagementService {

    private final UserRoleMapper userRoleMapper;
    private final CurrentUserContext currentUserContext;

    public UserRoleManagementService(UserRoleMapper userRoleMapper, CurrentUserContext currentUserContext) {
        this.userRoleMapper = userRoleMapper;
        this.currentUserContext = currentUserContext;
    }

    @Transactional(readOnly = true)
    public UserRoleListResponse listUserRoles(UserRoleSearchCondition condition) {
        return new UserRoleListResponse(
            userRoleMapper.selectUserRoles(condition),
            condition.page(),
            condition.size(),
            userRoleMapper.countUserRoles(condition),
            "SCR-USER-ROLE-MGMT",
            "R09"
        );
    }

    @Transactional
    public SaveUserRoleResponse saveUserRole(SaveUserRoleRequest request) {
        SessionPrincipal principal = currentUserContext.current()
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));
        Long userRoleId = request.userRoleId();
        if (userRoleId == null || !userRoleMapper.existsUserRole(userRoleId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "사용자 역할을 찾을 수 없습니다.");
        }
        LocalDate validTo = request.active() ? null : request.validTo();
        if (!request.active() && validTo == null) {
            validTo = LocalDate.of(2026, 12, 31);
        }
        userRoleMapper.updateUserRoleManagementFields(userRoleId, validTo, request.assignmentSource());
        userRoleMapper.insertAudit(
            "user_roles:" + userRoleId,
            principal.user().userId(),
            jsonAfterValue(userRoleId, request, validTo)
        );
        return new SaveUserRoleResponse(
            userRoleId,
            request.active(),
            validTo == null ? null : validTo.toString(),
            request.assignmentSource(),
            "사용자 역할 관리 저장이 완료되었습니다."
        );
    }

    private static String jsonAfterValue(Long userRoleId, SaveUserRoleRequest request, LocalDate validTo) {
        return "{\"userRoleId\":" + userRoleId
            + ",\"active\":" + request.active()
            + ",\"validTo\":" + nullableJson(validTo == null ? null : validTo.toString())
            + ",\"assignmentSource\":\"" + escapeJson(request.assignmentSource())
            + "\",\"reason\":\"" + escapeJson(request.reason()) + "\"}";
    }

    private static String nullableJson(String value) {
        return value == null ? "null" : "\"" + escapeJson(value) + "\"";
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
