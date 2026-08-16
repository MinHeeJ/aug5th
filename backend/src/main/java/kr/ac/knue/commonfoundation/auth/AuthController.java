package kr.ac.knue.commonfoundation.auth;

import java.time.Duration;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kr.ac.knue.commonfoundation.api.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class AuthController {

    private final AuthService authService;
    private final CurrentUserContext currentUserContext;

    public AuthController(AuthService authService, CurrentUserContext currentUserContext) {
        this.authService = authService;
        this.currentUserContext = currentUserContext;
    }

    @PostMapping("/api/auth/login")
    public ApiResponse<AuthenticatedUser> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest servletRequest,
        HttpServletResponse servletResponse
    ) {
        SessionPrincipal principal = authService.login(request, remoteAddress(servletRequest));
        servletResponse.addHeader(HttpHeaders.SET_COOKIE, sessionCookie(principal.sessionId(), Duration.ofHours(8)).toString());
        return ApiResponse.ok(principal.user());
    }

    @PostMapping("/api/auth/logout")
    public ApiResponse<Map<String, String>> logout(HttpServletResponse servletResponse) {
        SessionPrincipal principal = currentUserContext.current().orElseThrow();
        authService.logout(principal);
        servletResponse.addHeader(HttpHeaders.SET_COOKIE, sessionCookie("", Duration.ZERO).toString());
        return ApiResponse.ok(Map.of("status", "LOGOUT"));
    }

    @GetMapping("/api/auth/session")
    public ApiResponse<AuthenticatedUser> currentSession() {
        return ApiResponse.ok(currentUserContext.current().orElseThrow().user());
    }

    private static ResponseCookie sessionCookie(String value, Duration maxAge) {
        return ResponseCookie.from(AuthService.SESSION_COOKIE_NAME, value)
            .httpOnly(true)
            .path("/")
            .sameSite("Lax")
            .maxAge(maxAge)
            .build();
    }

    private static String remoteAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr() == null ? "0.0.0.0" : request.getRemoteAddr();
    }
}
