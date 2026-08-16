package kr.ac.knue.commonfoundation.privacy;

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
public class PrivacyPolicyManagementService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final PrivacyPolicyMapper privacyPolicyMapper;
    private final CurrentUserContext currentUserContext;

    public PrivacyPolicyManagementService(PrivacyPolicyMapper privacyPolicyMapper, CurrentUserContext currentUserContext) {
        this.privacyPolicyMapper = privacyPolicyMapper;
        this.currentUserContext = currentUserContext;
    }

    @Transactional(readOnly = true)
    public PrivacyPolicyListResponse listPrivacyPolicies(PrivacyPolicySearchCondition condition) {
        return new PrivacyPolicyListResponse(
            privacyPolicyMapper.selectPrivacyPolicies(condition),
            condition.page(),
            condition.size(),
            privacyPolicyMapper.countPrivacyPolicies(condition),
            "SCR-PRIVACY",
            "R09"
        );
    }

    @Transactional
    public SavePrivacyPolicyResponse savePrivacyPolicy(SavePrivacyPolicyRequest request) {
        SessionPrincipal principal = currentUserContext.current()
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));
        Long fieldPolicyId = request.fieldPolicyId();
        if (!privacyPolicyMapper.existsPrivacyPolicy(fieldPolicyId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "개인정보 정책을 찾을 수 없습니다.");
        }
        PrivacyPolicyListItem before = privacyPolicyMapper.selectPrivacyPolicy(fieldPolicyId);
        validatePrivacyPolicy(request, before);
        privacyPolicyMapper.updatePrivacyPolicy(
            fieldPolicyId,
            request.privacyGrade(),
            request.encryptionEnabled(),
            request.normalizedMaskingRule(),
            request.logExcluded()
        );
        PrivacyPolicyListItem saved = privacyPolicyMapper.selectPrivacyPolicy(fieldPolicyId);
        privacyPolicyMapper.insertAudit(
            "privacy_field_policies:" + fieldPolicyId,
            principal.user().userId(),
            jsonValue(before, null),
            jsonValue(saved, request.reason())
        );
        return new SavePrivacyPolicyResponse(
            saved.fieldPolicyId(),
            saved.fieldName(),
            saved.privacyGrade(),
            saved.encryptionEnabled(),
            saved.logExcluded(),
            "개인정보 관리 정책 저장이 완료되었습니다."
        );
    }

    private void validatePrivacyPolicy(SavePrivacyPolicyRequest request, PrivacyPolicyListItem before) {
        Map<String, String> fields = new LinkedHashMap<>();
        if (!before.fieldName().equals(request.normalizedFieldName())) {
            fields.put("fieldName", "필드명은 개인정보 정책 생명주기 식별자이므로 변경할 수 없습니다.");
        }
        if (privacyPolicyMapper.existsDuplicateFieldName(request.fieldPolicyId(), request.normalizedFieldName())) {
            fields.put("fieldName", "같은 필드명의 개인정보 정책이 이미 존재합니다.");
        }
        if (Boolean.TRUE.equals(request.encryptionEnabled()) && "PUBLIC".equals(request.privacyGrade())) {
            fields.put("privacyGrade", "암호화 대상은 PERSONAL 또는 SENSITIVE 등급이어야 합니다.");
        }
        if (Boolean.TRUE.equals(request.logExcluded()) && !Boolean.TRUE.equals(request.encryptionEnabled())) {
            fields.put("logExcluded", "로그 제외 정책은 암호화 대상 필드에만 적용할 수 있습니다.");
        }
        if (!fields.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "개인정보 정책 처리 조건을 확인하세요.", fields);
        }
    }

    private static String jsonValue(PrivacyPolicyListItem item, String reason) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("fieldPolicyId", item.fieldPolicyId());
        value.put("fieldName", item.fieldName());
        value.put("privacyGrade", item.privacyGrade());
        value.put("encryptionEnabled", item.encryptionEnabled());
        value.put("maskingRule", item.maskingRule());
        value.put("logExcluded", item.logExcluded());
        if (reason != null) {
            value.put("reason", reason);
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("개인정보 감사 로그 JSON 생성에 실패했습니다.", exception);
        }
    }
}
