package kr.ac.knue.commonfoundation.attachment;

import java.util.LinkedHashMap;
import java.util.Map;

import kr.ac.knue.commonfoundation.api.ApiException;
import kr.ac.knue.commonfoundation.auth.CurrentUserContext;
import kr.ac.knue.commonfoundation.auth.SessionPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttachmentManagementService {

    private final AttachmentMapper attachmentMapper;
    private final CurrentUserContext currentUserContext;

    public AttachmentManagementService(AttachmentMapper attachmentMapper, CurrentUserContext currentUserContext) {
        this.attachmentMapper = attachmentMapper;
        this.currentUserContext = currentUserContext;
    }

    @Transactional(readOnly = true)
    public AttachmentListResponse listAttachments(AttachmentSearchCondition condition) {
        return new AttachmentListResponse(
            attachmentMapper.selectAttachments(condition),
            condition.page(),
            condition.size(),
            attachmentMapper.countAttachments(condition),
            "SCR-ATTACHMENT",
            "R09"
        );
    }

    @Transactional
    public SaveAttachmentResponse saveAttachment(SaveAttachmentRequest request) {
        SessionPrincipal principal = requireCurrentUser();
        Long attachmentId = request.attachmentId();
        if (!attachmentMapper.existsAttachment(attachmentId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "첨부파일을 찾을 수 없습니다.");
        }
        AttachmentListItem before = attachmentMapper.selectAttachment(attachmentId);
        validateAttachment(request, before);
        return persistLogicalDelete(request, before, principal);
    }

    @Transactional
    public SaveAttachmentResponse deleteAttachment(long attachmentId, DeleteAttachmentRequest request) {
        SessionPrincipal principal = requireCurrentUser();
        if (!attachmentMapper.existsAttachment(attachmentId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "첨부파일을 찾을 수 없습니다.");
        }
        AttachmentListItem before = attachmentMapper.selectAttachment(attachmentId);
        DeleteAttachmentRequest normalizedRequest = request == null ? new DeleteAttachmentRequest(null, null) : request;
        SaveAttachmentRequest saveRequest = new SaveAttachmentRequest(
            Long.toString(attachmentId),
            before.businessKey(),
            true,
            normalizedRequest.normalizedDeleteReason(),
            normalizedRequest.normalizedReason()
        );
        validateAttachment(saveRequest, before);
        return persistLogicalDelete(saveRequest, before, principal);
    }

    @Transactional
    public AttachmentIntegrityCheckResponse runIntegrityCheck() {
        SessionPrincipal principal = requireCurrentUser();
        long totalCount = attachmentMapper.countAllAttachments();
        long abnormalCount = attachmentMapper.countAbnormalAttachments();
        attachmentMapper.insertAudit(
            "READ",
            "attachment_files:integrity-checks",
            principal.user().userId(),
            "{}",
            "{\"totalCount\":" + totalCount + ",\"abnormalCount\":" + abnormalCount + "}"
        );
        return new AttachmentIntegrityCheckResponse(
            totalCount,
            abnormalCount,
            abnormalCount == 0 ? "OK" : "NEEDS_REVIEW",
            "첨부파일 정합성 점검 결과가 기록되었습니다."
        );
    }

    private SaveAttachmentResponse persistLogicalDelete(
        SaveAttachmentRequest request,
        AttachmentListItem before,
        SessionPrincipal principal
    ) {
        Long attachmentId = request.attachmentId();
        boolean nextDeleted = Boolean.TRUE.equals(request.deleteRequested());
        attachmentMapper.updateLogicalDelete(attachmentId, nextDeleted, request.normalizedDeleteReason(), principal.user().userId());
        AttachmentListItem saved = attachmentMapper.selectAttachment(attachmentId);
        attachmentMapper.insertAudit(
            "UPDATE",
            "attachment_files:" + attachmentId,
            principal.user().userId(),
            jsonValue(before, null),
            jsonValue(saved, request.reason())
        );
        return new SaveAttachmentResponse(
            saved.attachmentId(),
            saved.businessKey(),
            saved.originalName(),
            saved.deleted(),
            saved.deleted() ? "LOGICAL_DELETE" : "LOGICAL_DELETE_CANCELLED",
            "첨부파일 관리 저장이 완료되었습니다."
        );
    }

    private SessionPrincipal requireCurrentUser() {
        return currentUserContext.current()
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));
    }

    private static void validateAttachment(SaveAttachmentRequest request, AttachmentListItem before) {
        Map<String, String> fields = new LinkedHashMap<>();
        if (!before.businessKey().equals(request.normalizedBusinessKey())) {
            fields.put("businessKey", "업무자료 키는 첨부파일 생명주기 식별자이므로 변경할 수 없습니다.");
        }
        if (Boolean.TRUE.equals(request.deleteRequested()) && request.normalizedDeleteReason() == null) {
            fields.put("deleteReason", "논리삭제 시 삭제 사유를 입력하세요.");
        }
        if (Boolean.TRUE.equals(request.deleteRequested()) && before.finalizedRecord()) {
            fields.put("deleteRequested", "평가확정 자료의 첨부파일은 논리삭제할 수 없습니다.");
        }
        if (!fields.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "첨부파일 처리 조건을 확인하세요.", fields);
        }
    }

    private static String jsonValue(AttachmentListItem item, String reason) {
        return "{\"attachmentId\":" + item.attachmentId()
            + ",\"businessKey\":\"" + escapeJson(item.businessKey())
            + "\",\"originalName\":\"" + escapeJson(item.originalName())
            + "\",\"storedName\":\"" + escapeJson(item.storedName())
            + "\",\"malwareScanResult\":\"" + escapeJson(item.malwareScanResult())
            + "\",\"deleted\":" + item.deleted()
            + ",\"integrityStatus\":\"" + escapeJson(item.integrityStatus())
            + "\"" + (reason == null ? "" : ",\"reason\":\"" + escapeJson(reason) + "\"")
            + "}";
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
