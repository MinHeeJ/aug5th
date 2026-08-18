package kr.ac.knue.cms.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import kr.ac.knue.cms.common.api.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class InternalAccountAuthenticationAdapter implements AuthenticationPort {
    private final AuthMapper authMapper;

    public InternalAccountAuthenticationAdapter(AuthMapper authMapper) {
        this.authMapper = authMapper;
    }

    @Override
    public AuthenticatedUser authenticate(String username, String password) {
        Map<String, Object> account = Optional.ofNullable(authMapper.findAccountByLoginId(username))
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED", "아이디 또는 비밀번호가 올바르지 않습니다."));
        if (!Boolean.TRUE.equals(account.get("systemEnabled")) || !"ACTIVE".equals(account.get("status"))) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_DISABLED", "사용할 수 없는 계정입니다.");
        }
        if (!sha256(password).equals(account.get("passwordHash"))) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED", "아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        UUID userId = (UUID) account.get("userId");
        return new AuthenticatedUser(userId, (String) account.get("loginId"), (String) account.get("staffName"), authMapper.findActiveRoleCodes(userId));
    }

    static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
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
