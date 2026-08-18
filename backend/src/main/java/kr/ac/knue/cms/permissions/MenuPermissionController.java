package kr.ac.knue.cms.permissions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
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
public class MenuPermissionController {
    private final MenuPermissionService service;
    private final SessionService sessionService;

    public MenuPermissionController(MenuPermissionService service, SessionService sessionService) {
        this.service = service;
        this.sessionService = sessionService;
    }

    @GetMapping("/api/admin/menu-permissions")
    public ApiResponse<List<Map<String, Object>>> listMenuPermissions(@RequestParam(required = false) String targetType,
                                                                       @RequestParam(required = false) String targetId,
                                                                       @RequestParam(required = false) String filter) {
        return ApiResponse.ok(service.list(targetType, targetId, filter));
    }

    @PutMapping("/api/admin/menu-permissions/{targetType}/{targetId}")
    public ApiResponse<List<Map<String, Object>>> saveMenuPermissions(@PathVariable String targetType,
                                                                       @PathVariable String targetId,
                                                                       @Valid @RequestBody MenuPermissionUpdateRequest request) {
        return ApiResponse.ok(service.save(targetType, targetId, request));
    }

    @GetMapping("/api/admin/menu-permissions/effective")
    public ApiResponse<List<Map<String, Object>>> getEffectiveMenuPermissions(HttpServletRequest request) {
        AuthenticatedUser actor = sessionService.findByRequest(request)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED", "인증 세션이 필요합니다."));
        return ApiResponse.ok(service.effective(actor));
    }
}
