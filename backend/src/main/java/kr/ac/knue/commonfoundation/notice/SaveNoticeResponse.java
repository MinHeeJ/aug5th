package kr.ac.knue.commonfoundation.notice;

import java.time.LocalDate;

public record SaveNoticeResponse(
    Long noticeId,
    String title,
    LocalDate postFrom,
    LocalDate postTo,
    String targetRoles,
    String targetOrganizations,
    boolean important,
    boolean enabled,
    String message
) {
}
