package kr.ac.knue.commonfoundation.session;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.knue.commonfoundation.api.ApiException;
import kr.ac.knue.commonfoundation.auth.CurrentUserContext;
import kr.ac.knue.commonfoundation.auth.SessionPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessionManagementService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final SessionMapper sessionMapper;
    private final CurrentUserContext currentUserContext;
    private final Clock clock;

    @Autowired
    public SessionManagementService(SessionMapper sessionMapper, CurrentUserContext currentUserContext) {
        this(sessionMapper, currentUserContext, Clock.systemUTC());
    }

    SessionManagementService(SessionMapper sessionMapper, CurrentUserContext currentUserContext, Clock clock) {
        this.sessionMapper = sessionMapper;
        this.currentUserContext = currentUserContext;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public SessionListResponse listActiveSessions(SessionSearchCondition condition) {
        return new SessionListResponse(
            sessionMapper.selectSessions(condition),
            condition.page(),
            condition.size(),
            sessionMapper.countSessions(condition),
            "SCR-SESSION",
            "R09"
        );
    }

    @Transactional
    public SaveSessionResponse saveActiveSession(SaveSessionRequest request) {
        return terminate(request.sessionId(), request.normalizedReason(), "접속현황 관리 세션 강제종료가 완료되었습니다.");
    }

    @Transactional
    public SaveSessionResponse terminateSession(String sessionId, TerminateSessionRequest request) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "세션 ID를 확인하세요.", Map.of("id", "세션 ID를 입력하세요."));
        }
        return terminate(sessionId.trim(), request.normalizedReason(), "세션 강제종료가 완료되었습니다.");
    }

    private SaveSessionResponse terminate(String sessionId, String reason, String message) {
        SessionPrincipal principal = currentUserContext.current()
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));
        if (!sessionMapper.existsSession(sessionId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "세션을 찾을 수 없습니다.");
        }
        SessionListItem before = sessionMapper.selectSession(sessionId);
        if (!"ACTIVE".equals(before.sessionStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "활성 세션만 강제종료할 수 있습니다.", Map.of("id", "이미 종료된 세션입니다."));
        }
        LocalDateTime now = LocalDateTime.now(clock);
        int updated = sessionMapper.terminateActiveSession(sessionId, now);
        if (updated != 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "세션 상태가 변경되어 강제종료할 수 없습니다.");
        }
        sessionMapper.insertForcedTermination(sessionId, reason, principal.user().userId(), now);
        SessionListItem after = sessionMapper.selectSession(sessionId);
        sessionMapper.insertAudit(
            "user_sessions:" + sessionId,
            principal.user().userId(),
            jsonValue(before, null),
            jsonValue(after, reason)
        );
        return new SaveSessionResponse(sessionId, after.sessionStatus(), "FORCED", message);
    }

    private static String jsonValue(SessionListItem item, String reason) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("sessionId", item.sessionId());
        value.put("userId", item.userId());
        value.put("loginAt", item.loginAt());
        value.put("lastActivityAt", item.lastActivityAt());
        value.put("ipAddress", item.ipAddress());
        value.put("sessionStatus", item.sessionStatus());
        value.put("latestTerminationType", item.latestTerminationType());
        if (reason != null) {
            value.put("reason", reason);
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("접속현황 감사 로그 JSON 생성에 실패했습니다.", exception);
        }
    }
}
