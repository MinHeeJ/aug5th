package kr.ac.knue.cms.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Map;
import kr.ac.knue.cms.common.api.ApiException;
import kr.ac.knue.cms.common.api.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    private final AuthenticationPort authenticationPort;
    private final SessionService sessionService;

    public AuthController(AuthenticationPort authenticationPort, SessionService sessionService) {
        this.authenticationPort = authenticationPort;
        this.sessionService = sessionService;
    }

    @GetMapping("/api/health")
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.ok(Map.of("status", "UP"));
    }

    @PostMapping("/api/auth/login")
    public ApiResponse<AuthenticatedUser> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthenticatedUser user = authenticationPort.authenticate(request.username(), request.password());
        String token = sessionService.create(user);
        ResponseCookie cookie = ResponseCookie.from(SessionService.COOKIE_NAME, token)
            .httpOnly(true)
            .secure(false)
            .sameSite("Lax")
            .path("/")
            .maxAge(8 * 60 * 60)
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ApiResponse.ok(user);
    }

    @PostMapping("/api/auth/logout")
    public ApiResponse<Map<String, Object>> logout(HttpServletRequest request, HttpServletResponse response) {
        String token = sessionService.extractToken(request)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED", "인증 세션이 필요합니다."));
        sessionService.logout(token);
        ResponseCookie cookie = ResponseCookie.from(SessionService.COOKIE_NAME, "")
            .httpOnly(true).secure(false).sameSite("Lax").path("/").maxAge(0).build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ApiResponse.message("로그아웃되었습니다.");
    }

    @GetMapping("/api/auth/me")
    public ApiResponse<AuthenticatedUser> me(HttpServletRequest request) {
        return ApiResponse.ok(sessionService.findByRequest(request)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED", "인증 세션이 필요합니다.")));
    }
}
