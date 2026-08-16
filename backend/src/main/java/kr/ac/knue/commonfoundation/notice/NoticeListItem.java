package kr.ac.knue.commonfoundation.notice;

import java.time.LocalDate;

public record NoticeListItem(
    Long noticeId,
    String title,
    String contentSummary,
    LocalDate postFrom,
    LocalDate postTo,
    String targetRoles,
    String targetOrganizations,
    boolean important,
    boolean enabled,
    int attachmentCount,
    String exposureRule,
    String readBoundary
) {
}
