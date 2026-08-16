package kr.ac.knue.commonfoundation.datascope;

import java.util.Map;

import kr.ac.knue.commonfoundation.api.ApiException;
import kr.ac.knue.commonfoundation.auth.CurrentUserContext;
import kr.ac.knue.commonfoundation.auth.SessionPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataScopeManagementService {

    private final DataScopeMapper dataScopeMapper;
    private final CurrentUserContext currentUserContext;

    public DataScopeManagementService(DataScopeMapper dataScopeMapper, CurrentUserContext currentUserContext) {
        this.dataScopeMapper = dataScopeMapper;
        this.currentUserContext = currentUserContext;
    }

    @Transactional(readOnly = true)
    public DataScopeListResponse listDataScopes(DataScopeSearchCondition condition) {
        return new DataScopeListResponse(
            dataScopeMapper.selectDataScopes(condition),
            condition.page(),
            condition.size(),
            dataScopeMapper.countDataScopes(condition),
            "SCR-DATA-SCOPE",
            "R09"
        );
    }

    @Transactional
    public SaveDataScopeResponse saveDataScope(SaveDataScopeRequest request) {
        SessionPrincipal principal = currentUserContext.current()
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));
        Long dataScopeId = request.dataScopeId();
        if (dataScopeId == null || !dataScopeMapper.existsDataScope(dataScopeId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "데이터 범위 권한을 찾을 수 없습니다.");
        }
        if (dataScopeMapper.roleIdentityMismatch(dataScopeId, request.roleCode())) {
            throw new ApiException(HttpStatus.CONFLICT, "CONFLICT", "역할 코드는 데이터 범위 권한 생명주기 식별자와 함께 변경할 수 없습니다.", Map.of("id", "선택한 데이터 범위 권한과 역할 정보가 일치하지 않습니다."));
        }
        dataScopeMapper.updateDataScope(dataScopeId, blankToNull(request.scopeType()), blankToNull(request.organizationCode()), blankToNull(request.businessArea()));
        dataScopeMapper.insertAudit(
            "data_scope_permissions:" + dataScopeId,
            principal.user().userId(),
            jsonAfterValue(dataScopeId, request)
        );
        return new SaveDataScopeResponse(
            dataScopeId,
            request.roleCode(),
            request.scopeType(),
            blankToNull(request.organizationCode()),
            blankToNull(request.businessArea()),
            "데이터 범위 권한 저장이 완료되었습니다."
        );
    }

    private static String jsonAfterValue(Long dataScopeId, SaveDataScopeRequest request) {
        return "{\"dataScopeId\":" + dataScopeId
            + ",\"roleCode\":\"" + escapeJson(request.roleCode())
            + "\",\"scopeType\":\"" + escapeJson(request.scopeType())
            + "\",\"organizationCode\":" + nullableJson(request.organizationCode())
            + ",\"businessArea\":" + nullableJson(request.businessArea())
            + ",\"reason\":\"" + escapeJson(request.reason()) + "\"}";
    }

    private static String nullableJson(String value) {
        String normalized = blankToNull(value);
        return normalized == null ? "null" : "\"" + escapeJson(normalized) + "\"";
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
