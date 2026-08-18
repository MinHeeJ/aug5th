package kr.ac.knue.cms.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import kr.ac.knue.cms.auth.AuthenticatedUser;
import kr.ac.knue.cms.auth.SessionService;
import kr.ac.knue.cms.common.api.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class MenuPermissionInterceptor implements HandlerInterceptor {
    private final SessionService sessionService;
    private final MenuPermissionMapper menuPermissionMapper;
    private final ObjectMapper objectMapper;

    public MenuPermissionInterceptor(SessionService sessionService, MenuPermissionMapper menuPermissionMapper, ObjectMapper objectMapper) {
        this.sessionService = sessionService;
        this.menuPermissionMapper = menuPermissionMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String path = request.getRequestURI();
        if ("/api/health".equals(path) || "/api/auth/login".equals(path)) {
            return true;
        }
        if (!path.startsWith("/api/")) {
            return true;
        }
        AuthenticatedUser user = sessionService.findByRequest(request).orElse(null);
        if (user == null) {
            writeError(response, HttpStatus.UNAUTHORIZED, ApiError.of("AUTHENTICATION_REQUIRED", "인증 세션이 필요합니다."));
            return false;
        }
        if (path.startsWith("/api/auth/")) {
            return true;
        }
        if (path.startsWith("/api/admin/") || path.startsWith("/api/navigation/")) {
            if (!user.hasRole("R09")) {
                writeError(response, HttpStatus.FORBIDDEN, ApiError.of("ACCESS_DENIED", "접근 권한이 없습니다."));
                return false;
            }
            if (path.startsWith("/api/admin/") && menuPermissionMapper.isKnownMenuPath(path)
                && !menuPermissionMapper.hasMenuPermission(user.roleCodes().toArray(String[]::new), path)) {
                writeError(response, HttpStatus.FORBIDDEN, ApiError.of("MENU_ACCESS_DENIED", "메뉴 접근 권한이 없습니다."));
                return false;
            }
        }
        return true;
    }

    private void writeError(HttpServletResponse response, HttpStatus status, ApiError error) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), error);
    }
}
