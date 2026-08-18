package kr.ac.knue.cms.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SessionService {
    public static final String COOKIE_NAME = "SESSION";
    private final AuthMapper authMapper;

    public SessionService(AuthMapper authMapper) {
        this.authMapper = authMapper;
    }

    public String create(AuthenticatedUser user) {
        String token = UUID.randomUUID() + ":" + UUID.randomUUID();
        authMapper.insertSession(UUID.randomUUID(), user.userId(), hashToken(token), LocalDateTime.now().plusHours(8));
        return token;
    }

    public Optional<AuthenticatedUser> findByToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        Map<String, Object> row = authMapper.findActiveSession(hashToken(token));
        if (row == null) {
            return Optional.empty();
        }
        UUID userId = (UUID) row.get("userId");
        return Optional.of(new AuthenticatedUser(userId, (String) row.get("loginId"),
            (String) row.get("staffName"), authMapper.findActiveRoleCodes(userId)));
    }

    public Optional<AuthenticatedUser> findByRequest(HttpServletRequest request) {
        return findByToken(extractToken(request).orElse(null));
    }

    public Optional<String> extractToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies).filter(cookie -> COOKIE_NAME.equals(cookie.getName())).map(Cookie::getValue).findFirst();
    }

    public void logout(String token) {
        if (token != null && !token.isBlank()) {
            authMapper.logout(hashToken(token));
        }
    }

    static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
