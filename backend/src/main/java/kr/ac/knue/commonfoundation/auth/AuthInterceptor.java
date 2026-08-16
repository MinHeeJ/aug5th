package kr.ac.knue.commonfoundation.auth;

import java.io.IOException;
import java.util.Arrays;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.ac.knue.commonfoundation.api.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;
    private final CurrentUserContext currentUserContext;

    public AuthInterceptor(AuthService authService, CurrentUserContext currentUserContext) {
        this.authService = authService;
        this.currentUserContext = currentUserContext;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (isOpenEndpoint(request)) {
            return true;
        }
        SessionPrincipal principal = authService.authenticate(sessionCookie(request));
        if (request.getRequestURI().startsWith("/api/admin") && !principal.user().hasRole("R09")) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "권한이 없습니다.");
        }
        currentUserContext.set(principal);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
        currentUserContext.clear();
    }

    private static boolean isOpenEndpoint(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return "/api/health".equals(uri) || ("/api/auth/login".equals(uri) && "POST".equals(request.getMethod()));
    }

    private static String sessionCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
            .filter(cookie -> AuthService.SESSION_COOKIE_NAME.equals(cookie.getName()))
            .map(Cookie::getValue)
            .findFirst()
            .orElse(null);
    }
}
