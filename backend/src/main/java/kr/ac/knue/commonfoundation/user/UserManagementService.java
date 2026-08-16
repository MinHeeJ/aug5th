package kr.ac.knue.commonfoundation.user;

import java.time.Clock;
import java.time.LocalDateTime;

import kr.ac.knue.commonfoundation.api.ApiException;
import kr.ac.knue.commonfoundation.auth.CurrentUserContext;
import kr.ac.knue.commonfoundation.auth.SessionPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserManagementService {

    private final UserMapper userMapper;
    private final CurrentUserContext currentUserContext;
    private final Clock clock;

    @Autowired
    public UserManagementService(UserMapper userMapper, CurrentUserContext currentUserContext) {
        this(userMapper, currentUserContext, Clock.systemUTC());
    }

    UserManagementService(UserMapper userMapper, CurrentUserContext currentUserContext, Clock clock) {
        this.userMapper = userMapper;
        this.currentUserContext = currentUserContext;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public UserListResponse listUsers(UserSearchCondition condition) {
        return new UserListResponse(
            userMapper.selectUsers(condition),
            condition.page(),
            condition.size(),
            userMapper.countUsers(condition),
            "SCR-USER-MGMT",
            "R09"
        );
    }

    @Transactional
    public SaveUserResponse saveUser(SaveUserRequest request) {
        SessionPrincipal principal = currentUserContext.current()
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));
        if (!userMapper.existsUser(request.id())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "사용자를 찾을 수 없습니다.");
        }
        String roleSummary = request.roleSummary() == null || request.roleSummary().isBlank() ? null : request.roleSummary().trim();
        userMapper.updateUserManagementFields(request.id(), request.enabled(), request.status(), roleSummary, LocalDateTime.now(clock));
        userMapper.insertAudit(
            "user_accounts:" + request.id(),
            principal.user().userId(),
            jsonAfterValue(request, roleSummary)
        );
        return new SaveUserResponse(request.id(), request.enabled(), request.status(), "사용자 관리 저장이 완료되었습니다.");
    }

    private static String jsonAfterValue(SaveUserRequest request, String roleSummary) {
        return "{\"enabled\":" + request.enabled()
            + ",\"status\":\"" + escapeJson(request.status())
            + "\",\"roleSummary\":" + nullableJson(roleSummary)
            + ",\"reason\":\"" + escapeJson(request.reason()) + "\"}";
    }

    private static String nullableJson(String value) {
        return value == null ? "null" : "\"" + escapeJson(value) + "\"";
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
