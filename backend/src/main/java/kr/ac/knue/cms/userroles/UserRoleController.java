package kr.ac.knue.cms.userroles;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kr.ac.knue.cms.auth.AuthenticatedUser;
import kr.ac.knue.cms.auth.SessionService;
import kr.ac.knue.cms.common.api.ApiException;
import kr.ac.knue.cms.common.api.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserRoleController {
    private final UserRoleService userRoleService;
    private final SessionService sessionService;

    public UserRoleController(UserRoleService userRoleService, SessionService sessionService) {
        this.userRoleService = userRoleService;
        this.sessionService = sessionService;
    }

    @GetMapping("/api/admin/user-roles")
    public ApiResponse<List<Map<String, Object>>> listUserRoles(@RequestParam(required = false) UUID userId,
                                                                 @RequestParam(required = false) String roleCode,
                                                                 @RequestParam(required = false) String filter) {
        return ApiResponse.ok(userRoleService.listUserRoles(userId, roleCode, filter));
    }

    @PutMapping("/api/admin/user-roles/{userId}")
    public ApiResponse<List<Map<String, Object>>> saveUserRoles(@PathVariable UUID userId,
                                                                 @Valid @RequestBody UserRolesUpdateRequest request,
                                                                 HttpServletRequest httpRequest) {
        AuthenticatedUser actor = sessionService.findByRequest(httpRequest)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED", "인증 세션이 필요합니다."));
        return ApiResponse.ok(userRoleService.saveUserRoles(userId, request, actor));
    }
}
