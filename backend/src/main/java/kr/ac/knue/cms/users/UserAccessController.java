package kr.ac.knue.cms.users;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import kr.ac.knue.cms.auth.SessionService;
import kr.ac.knue.cms.common.api.ApiException;
import kr.ac.knue.cms.common.api.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserAccessController {
    private final UserService userService;
    private final SessionService sessionService;

    public UserAccessController(UserService userService, SessionService sessionService) {
        this.userService = userService;
        this.sessionService = sessionService;
    }

    @PatchMapping("/api/admin/users/{userId}/system-access")
    public ApiResponse<UserSummary> updateSystemAccess(@PathVariable UUID userId,
                                                       @Valid @RequestBody UserSystemAccessRequest request,
                                                       HttpServletRequest httpRequest) {
        return ApiResponse.ok(userService.updateSystemAccess(userId, request, sessionService.findByRequest(httpRequest)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED", "인증 세션이 필요합니다."))));
    }
}
