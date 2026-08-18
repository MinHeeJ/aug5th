package kr.ac.knue.commonfoundation.auditlog;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.knue.commonfoundation.api.ApiException;
import kr.ac.knue.commonfoundation.auth.CurrentUserContext;
import kr.ac.knue.commonfoundation.auth.SessionPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogManagementService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final AuditLogMapper auditLogMapper;
    private final CurrentUserContext currentUserContext;

    public AuditLogManagementService(AuditLogMapper auditLogMapper, CurrentUserContext currentUserContext) {
        this.auditLogMapper = auditLogMapper;
        this.currentUserContext = currentUserContext;
    }

    @Transactional(readOnly = true)
    public AuditLogListResponse listAuditLogs(AuditLogSearchCondition condition) {
        return new AuditLogListResponse(
            auditLogMapper.selectAuditLogs(condition),
            condition.page(),
            condition.size(),
            auditLogMapper.countAuditLogs(condition),
            "SCR-AUDIT-LOG",
            "R09"
        );
    }

    @Transactional
    public SaveAuditLogResponse saveAuditLog(SaveAuditLogRequest request) {
        SessionPrincipal principal = currentUserContext.current()
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));
        long selectedAuditLogId = request.auditLogId();
        if (!auditLogMapper.existsAuditLog(selectedAuditLogId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "감사 로그를 찾을 수 없습니다.");
        }
        AuditLogListItem selected = auditLogMapper.selectAuditLog(selectedAuditLogId);
        String targetKey = "audit_logs:" + selectedAuditLogId;
        GeneratedAuditLogId generated = new GeneratedAuditLogId();
        auditLogMapper.insertAuditManagementLog(
            generated,
            targetKey,
            principal.user().userId(),
            jsonValue(selected),
            jsonConfirmation(selectedAuditLogId, request.normalizedReason())
        );
        return new SaveAuditLogResponse(generated.getAuditLogId(), targetKey, "SUCCESS", "감사 로그 관리 확인 이력이 기록되었습니다.");
    }

    private static String jsonValue(AuditLogListItem item) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("auditLogId", item.auditLogId());
        value.put("logType", item.logType());
        value.put("targetKey", item.targetKey());
        value.put("actorId", item.actorId());
        value.put("beforeValue", item.beforeValue());
        value.put("afterValue", item.afterValue());
        value.put("result", item.result());
        return writeJson(value);
    }

    private static String jsonConfirmation(long auditLogId, String reason) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("confirmedAuditLogId", auditLogId);
        value.put("reason", reason);
        value.put("rule", "감사 로그 원문은 불변이며 확인 이력만 추가합니다.");
        return writeJson(value);
    }

    private static String writeJson(Map<String, Object> value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("감사 로그 JSON 생성에 실패했습니다.", exception);
        }
    }
}
