package kr.ac.knue.cms.users;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import kr.ac.knue.cms.auth.AuthenticatedUser;
import kr.ac.knue.cms.common.api.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public List<UserSummary> listUsers(String filter, String staffId, String staffName, String organizationCode,
                                       String rankTitle, String employmentStatus, String roleCode,
                                       Boolean systemEnabled, int page, int size) {
        int safeSize = Math.max(1, Math.min(size, 100));
        int offset = Math.max(0, page) * safeSize;
        return userMapper.findUsers(filter, staffId, staffName, organizationCode, rankTitle, employmentStatus,
                roleCode, systemEnabled, safeSize, offset).stream().map(this::toSummary).toList();
    }

    @Transactional
    public UserSummary updateSystemAccess(UUID userId, UserSystemAccessRequest request, AuthenticatedUser actor) {
        Map<String, Object> before = Optional.ofNullable(userMapper.findUser(userId))
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));
        userMapper.updateSystemEnabled(userId, request.isSystemEnabled());
        userMapper.revokeManualRoles(userId, request.changeReason());
        for (String roleCode : request.roleCodes()) {
            if (!roleCode.matches("R0[1-9]")) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "역할 코드를 확인해 주세요.", Map.of("roleCodes", "R01~R09만 허용됩니다."));
            }
            userMapper.insertManualRole(userId, roleCode, actor.userId(), request.changeReason());
        }
        return Optional.ofNullable(userMapper.findUser(userId)).map(this::toSummary)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));
    }

    private UserSummary toSummary(Map<String, Object> row) {
        UUID userId = (UUID) row.get("userId");
        return new UserSummary(
            userId,
            (String) row.get("loginId"),
            (String) row.get("staffId"),
            (String) row.get("staffName"),
            (String) row.get("organizationCode"),
            (String) row.get("rankTitle"),
            (String) row.get("employmentStatus"),
            (String) row.get("positionTitle"),
            toLocalDate(row.get("retirementDate")),
            toLocalDateTime(row.get("lastSyncedAt")),
            Boolean.TRUE.equals(row.get("isSystemEnabled")),
            userMapper.findActiveRoleCodes(userId)
        );
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        return LocalDate.parse(value.toString());
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        return LocalDateTime.parse(value.toString().replace(" ", "T"));
    }
}
