package kr.ac.knue.commonfoundation.codedetail;

import kr.ac.knue.commonfoundation.api.ApiException;
import kr.ac.knue.commonfoundation.auth.CurrentUserContext;
import kr.ac.knue.commonfoundation.auth.SessionPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CodeDetailManagementService {

    private final CodeDetailMapper codeDetailMapper;
    private final CurrentUserContext currentUserContext;

    public CodeDetailManagementService(CodeDetailMapper codeDetailMapper, CurrentUserContext currentUserContext) {
        this.codeDetailMapper = codeDetailMapper;
        this.currentUserContext = currentUserContext;
    }

    @Transactional(readOnly = true)
    public CodeDetailListResponse listCodeDetails(CodeDetailSearchCondition condition) {
        return new CodeDetailListResponse(
            codeDetailMapper.selectCodeDetails(condition),
            condition.page(),
            condition.size(),
            codeDetailMapper.countCodeDetails(condition),
            "SCR-CODE-DETAIL",
            "R09"
        );
    }

    @Transactional
    public SaveCodeDetailResponse saveCodeDetail(SaveCodeDetailRequest request) {
        SessionPrincipal principal = currentUserContext.current()
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));
        long codeDetailId = request.codeDetailId();
        if (!codeDetailMapper.existsCodeDetail(codeDetailId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "상세코드를 찾을 수 없습니다.");
        }
        String parentCodeValue = blankToNull(request.parentCodeValue());
        codeDetailMapper.updateCodeDetail(codeDetailId, request.codeName(), parentCodeValue, request.displayOrder());
        CodeDetailListItem saved = codeDetailMapper.selectCodeDetail(codeDetailId);
        codeDetailMapper.insertAudit(
            "UPDATE",
            "code_details:" + codeDetailId,
            principal.user().userId(),
            jsonAfterValue(saved, request.reason())
        );
        return new SaveCodeDetailResponse(
            saved.codeDetailId(),
            saved.groupId(),
            saved.codeValue(),
            saved.codeName(),
            saved.displayOrder(),
            saved.active(),
            "상세코드 관리 저장이 완료되었습니다."
        );
    }

    private static String jsonAfterValue(CodeDetailListItem saved, String reason) {
        return "{\"codeDetailId\":" + saved.codeDetailId()
            + ",\"groupId\":\"" + escapeJson(saved.groupId())
            + "\",\"codeValue\":\"" + escapeJson(saved.codeValue())
            + "\",\"codeName\":\"" + escapeJson(saved.codeName())
            + "\",\"parentCodeValue\":" + nullableJson(saved.parentCodeValue())
            + ",\"displayOrder\":" + saved.displayOrder()
            + ",\"active\":" + saved.active()
            + ",\"reason\":\"" + escapeJson(reason) + "\"}";
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
