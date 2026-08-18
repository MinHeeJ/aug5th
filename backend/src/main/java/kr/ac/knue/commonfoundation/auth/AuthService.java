package kr.ac.knue.commonfoundation.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import kr.ac.knue.commonfoundation.api.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    static final String SESSION_COOKIE_NAME = "KNUE_SESSION_ID";

    private final AuthMapper authMapper;
    private final Clock clock;

    @Autowired
    public AuthService(AuthMapper authMapper) {
        this(authMapper, Clock.systemUTC());
    }

    AuthService(AuthMapper authMapper, Clock clock) {
        this.authMapper = authMapper;
        this.clock = clock;
    }

    @Transactional
    public SessionPrincipal login(LoginRequest request, String ipAddress) {
        LoginUserRecord user = authMapper.findLoginUser(request.userId());
        if (user == null || !Boolean.TRUE.equals(user.enabled()) || !"ACTIVE".equals(user.status())) {
            throw unauthorized();
        }
        if (user.passwordHash() == null || !user.passwordHash().equals(sha256(request.password()))) {
            throw unauthorized();
        }
        List<String> roles = authMapper.findRoles(user.userId());
        if (roles.isEmpty()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "권한이 없습니다.");
        }
        String sessionId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now(clock);
        authMapper.insertSession(sessionId, user.userId(), now, ipAddress);
        authMapper.insertAudit("LOGIN", sessionId, user.userId(), "{\"status\":\"ACTIVE\"}", "SUCCESS");
        return new SessionPrincipal(sessionId, new AuthenticatedUser(user.userId(), roles, defaultScope(user.userId())));
    }

    public SessionPrincipal authenticate(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw unauthorized();
        }
        String userId = authMapper.findActiveSessionUserId(sessionId);
        if (userId == null) {
            throw unauthorized();
        }
        List<String> roles = authMapper.findRoles(userId);
        if (roles.isEmpty()) {
            throw unauthorized();
        }
        return new SessionPrincipal(sessionId, new AuthenticatedUser(userId, roles, defaultScope(userId)));
    }

    @Transactional
    public void logout(SessionPrincipal principal) {
        LocalDateTime now = LocalDateTime.now(clock);
        authMapper.updateSessionStatus(principal.sessionId(), "LOGOUT", now);
        authMapper.insertTermination(principal.sessionId(), "LOGOUT", "사용자 로그아웃", principal.user().userId(), now);
        authMapper.insertAudit("LOGOUT", principal.sessionId(), principal.user().userId(), "{\"status\":\"LOGOUT\"}", "SUCCESS");
    }

    private String defaultScope(String userId) {
        String dataScope = authMapper.findDataScope(userId);
        return dataScope == null || dataScope.isBlank() ? "SELF" : dataScope;
    }

    private static ApiException unauthorized() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다.");
    }

    private static String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 digest is unavailable", exception);
        }
    }
}
