package kr.ac.knue.cms.menus;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import kr.ac.knue.cms.auth.AuthenticatedUser;
import kr.ac.knue.cms.auth.SessionService;
import kr.ac.knue.cms.common.api.ApiException;
import kr.ac.knue.cms.common.api.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NavigationController {
    private final SessionService sessionService;
    private final NavigationService navigationService;

    public NavigationController(SessionService sessionService, NavigationService navigationService) {
        this.sessionService = sessionService;
        this.navigationService = navigationService;
    }

    @GetMapping("/api/navigation/menus")
    public ApiResponse<List<NavigationMenu>> listMenus(HttpServletRequest request) {
        AuthenticatedUser user = sessionService.findByRequest(request)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED", "인증 세션이 필요합니다."));
        return ApiResponse.ok(navigationService.listVisibleMenus(user.roleCodes()));
    }
}
