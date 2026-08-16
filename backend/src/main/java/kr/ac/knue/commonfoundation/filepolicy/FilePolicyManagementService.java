package kr.ac.knue.commonfoundation.filepolicy;

import java.util.LinkedHashMap;
import java.util.Map;

import kr.ac.knue.commonfoundation.api.ApiException;
import kr.ac.knue.commonfoundation.auth.CurrentUserContext;
import kr.ac.knue.commonfoundation.auth.SessionPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FilePolicyManagementService {

    private final FilePolicyMapper filePolicyMapper;
    private final CurrentUserContext currentUserContext;

    public FilePolicyManagementService(FilePolicyMapper filePolicyMapper, CurrentUserContext currentUserContext) {
        this.filePolicyMapper = filePolicyMapper;
        this.currentUserContext = currentUserContext;
    }

    @Transactional(readOnly = true)
    public FilePolicyListResponse listFilePolicies(FilePolicySearchCondition condition) {
        return new FilePolicyListResponse(
            filePolicyMapper.selectFilePolicies(condition),
            condition.page(),
            condition.size(),
            filePolicyMapper.countFilePolicies(condition),
            "SCR-FILE-POLICY",
            "R09"
        );
    }

    @Transactional
    public SaveFilePolicyResponse saveFilePolicy(SaveFilePolicyRequest request) {
        SessionPrincipal principal = currentUserContext.current()
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));
        Long filePolicyId = request.filePolicyId();
        if (!filePolicyMapper.existsFilePolicy(filePolicyId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "파일정책을 찾을 수 없습니다.");
        }
        validatePolicy(request);
        FilePolicyListItem before = filePolicyMapper.selectFilePolicy(filePolicyId);
        filePolicyMapper.updateFilePolicy(
            filePolicyId,
            request.normalizedAllowedExtensions(),
            request.maxFileSizeMb(),
            request.maxFileCount(),
            request.maxTotalSizeMb(),
            request.maxFilenameLength(),
            request.malwareScanEnabled(),
            request.enabled()
        );
        FilePolicyListItem saved = filePolicyMapper.selectFilePolicy(filePolicyId);
        filePolicyMapper.insertAudit(
            "UPDATE",
            "file_policies:" + filePolicyId,
            principal.user().userId(),
            jsonValue(before, null),
            jsonValue(saved, request.reason())
        );
        return new SaveFilePolicyResponse(
            saved.filePolicyId(),
            saved.businessArea(),
            saved.allowedExtensions(),
            saved.maxFileSizeMb(),
            saved.maxFileCount(),
            saved.maxTotalSizeMb(),
            saved.maxFilenameLength(),
            saved.malwareScanEnabled(),
            saved.enabled(),
            "파일정책 관리 저장이 완료되었습니다."
        );
    }

    private static void validatePolicy(SaveFilePolicyRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        if (request.maxTotalSizeMb() < request.maxFileSizeMb()) {
            fields.put("maxTotalSizeMb", "전체용량은 단일 파일 최대용량보다 작을 수 없습니다.");
        }
        for (String extension : request.normalizedAllowedExtensions().split(",")) {
            if ("exe".equals(extension) || "sh".equals(extension) || "bat".equals(extension)) {
                fields.put("allowedExtensions", "실행 파일 확장자는 허용할 수 없습니다.");
                break;
            }
        }
        if (!fields.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "파일정책 값을 확인하세요.", fields);
        }
    }

    private static String jsonValue(FilePolicyListItem item, String reason) {
        return "{\"filePolicyId\":" + item.filePolicyId()
            + ",\"businessArea\":\"" + escapeJson(item.businessArea())
            + "\",\"allowedExtensions\":\"" + escapeJson(item.allowedExtensions())
            + "\",\"maxFileSizeMb\":" + item.maxFileSizeMb()
            + ",\"maxFileCount\":" + item.maxFileCount()
            + ",\"maxTotalSizeMb\":" + item.maxTotalSizeMb()
            + ",\"maxFilenameLength\":" + item.maxFilenameLength()
            + ",\"malwareScanEnabled\":" + item.malwareScanEnabled()
            + ",\"enabled\":" + item.enabled()
            + (reason == null ? "" : ",\"reason\":\"" + escapeJson(reason) + "\"")
            + "}";
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
