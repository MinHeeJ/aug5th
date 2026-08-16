package kr.ac.knue.commonfoundation.codegroup;

import kr.ac.knue.commonfoundation.api.ApiException;
import kr.ac.knue.commonfoundation.auth.CurrentUserContext;
import kr.ac.knue.commonfoundation.auth.SessionPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CodeGroupManagementService {

    private final CodeGroupMapper codeGroupMapper;
    private final CurrentUserContext currentUserContext;

    public CodeGroupManagementService(CodeGroupMapper codeGroupMapper, CurrentUserContext currentUserContext) {
        this.codeGroupMapper = codeGroupMapper;
        this.currentUserContext = currentUserContext;
    }

    @Transactional(readOnly = true)
    public CodeGroupListResponse listCodeGroups(CodeGroupSearchCondition condition) {
        return new CodeGroupListResponse(
            codeGroupMapper.selectCodeGroups(condition),
            condition.page(),
            condition.size(),
            codeGroupMapper.countCodeGroups(condition),
            "SCR-CODE-GROUP",
            "R09"
        );
    }

    @Transactional
    public SaveCodeGroupResponse saveCodeGroup(SaveCodeGroupRequest request) {
        SessionPrincipal principal = currentUserContext.current()
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));
        String groupId = request.groupId();
        String description = blankToNull(request.description());
        String managingDepartment = blankToNull(request.managingDepartment());
        boolean exists = codeGroupMapper.existsCodeGroup(groupId);
        if (exists) {
            codeGroupMapper.updateCodeGroup(groupId, request.groupName(), description, managingDepartment, request.enabled());
        } else {
            codeGroupMapper.insertCodeGroup(groupId, request.groupName(), description, managingDepartment, request.enabled());
        }
        codeGroupMapper.insertAudit(
            exists ? "UPDATE" : "CREATE",
            "code_groups:" + groupId,
            principal.user().userId(),
            jsonAfterValue(groupId, description, managingDepartment, request)
        );
        return new SaveCodeGroupResponse(groupId, request.groupName(), managingDepartment, request.enabled(), "코드그룹 관리 저장이 완료되었습니다.");
    }

    private static String jsonAfterValue(String groupId, String description, String managingDepartment, SaveCodeGroupRequest request) {
        return "{\"groupId\":\"" + escapeJson(groupId)
            + "\",\"groupName\":\"" + escapeJson(request.groupName())
            + "\",\"description\":" + nullableJson(description)
            + ",\"managingDepartment\":" + nullableJson(managingDepartment)
            + ",\"enabled\":" + request.enabled()
            + ",\"reason\":\"" + escapeJson(request.reason()) + "\"}";
    }

    private static String nullableJson(String value) {
        String normalized = blankToNull(value);
        return normalized == null ? "null" : "\"" + escapeJson(normalized) + "\"";
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
