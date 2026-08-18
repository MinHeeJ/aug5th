package kr.ac.knue.commonfoundation.notice;

import java.util.LinkedHashMap;
import java.util.Map;

import kr.ac.knue.commonfoundation.api.ApiException;
import kr.ac.knue.commonfoundation.auth.CurrentUserContext;
import kr.ac.knue.commonfoundation.auth.SessionPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NoticeManagementService {

    private final NoticeMapper noticeMapper;
    private final CurrentUserContext currentUserContext;

    public NoticeManagementService(NoticeMapper noticeMapper, CurrentUserContext currentUserContext) {
        this.noticeMapper = noticeMapper;
        this.currentUserContext = currentUserContext;
    }

    @Transactional(readOnly = true)
    public NoticeListResponse listNotices(NoticeSearchCondition condition) {
        return new NoticeListResponse(
            noticeMapper.selectNotices(condition),
            condition.page(),
            condition.size(),
            noticeMapper.countNotices(condition),
            "SCR-NOTICE",
            "R09"
        );
    }

    @Transactional
    public SaveNoticeResponse saveNotice(SaveNoticeRequest request) {
        SessionPrincipal principal = currentUserContext.current()
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));
        Long noticeId = request.noticeId();
        if (!noticeMapper.existsNotice(noticeId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "공지사항을 찾을 수 없습니다.");
        }
        validateNotice(request);
        NoticeListItem before = noticeMapper.selectNotice(noticeId);
        noticeMapper.updateNotice(
            noticeId,
            request.normalizedTitle(),
            request.postFrom(),
            request.postTo(),
            request.normalizedTargetRoles(),
            request.normalizedTargetOrganizations(),
            request.important(),
            request.enabled()
        );
        NoticeListItem saved = noticeMapper.selectNotice(noticeId);
        noticeMapper.insertAudit(
            "UPDATE",
            "notices:" + noticeId,
            principal.user().userId(),
            jsonValue(before, null),
            jsonValue(saved, request.reason())
        );
        return new SaveNoticeResponse(
            saved.noticeId(),
            saved.title(),
            saved.postFrom(),
            saved.postTo(),
            saved.targetRoles(),
            saved.targetOrganizations(),
            saved.important(),
            saved.enabled(),
            "공지사항 관리 저장이 완료되었습니다."
        );
    }

    private static void validateNotice(SaveNoticeRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        if (request.postFrom() != null && request.postTo() != null && request.postTo().isBefore(request.postFrom())) {
            fields.put("postTo", "게시 종료일은 게시 시작일보다 빠를 수 없습니다.");
        }
        if (request.normalizedTargetRoles() == null && request.normalizedTargetOrganizations() == null) {
            fields.put("targetRoles", "대상 역할 또는 대상 조직 중 하나 이상을 입력하세요.");
        }
        if (!fields.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "공지사항 게시 조건을 확인하세요.", fields);
        }
    }

    private static String jsonValue(NoticeListItem item, String reason) {
        return "{\"noticeId\":" + item.noticeId()
            + ",\"title\":\"" + escapeJson(item.title())
            + "\",\"postFrom\":\"" + item.postFrom()
            + "\",\"postTo\":\"" + item.postTo()
            + "\",\"targetRoles\":\"" + escapeJson(nullToEmpty(item.targetRoles()))
            + "\",\"targetOrganizations\":\"" + escapeJson(nullToEmpty(item.targetOrganizations()))
            + "\",\"important\":" + item.important()
            + ",\"enabled\":" + item.enabled()
            + ",\"attachmentCount\":" + item.attachmentCount()
            + (reason == null ? "" : ",\"reason\":\"" + escapeJson(reason) + "\"")
            + "}";
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
