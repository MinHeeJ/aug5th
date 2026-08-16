package kr.ac.knue.commonfoundation.notice;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SaveNoticeRequest(
    @NotBlank(message = "공지사항 ID를 입력하세요.")
    @Pattern(regexp = "^[0-9]+$", message = "공지사항 ID는 숫자여야 합니다.")
    String id,
    @NotBlank(message = "제목을 입력하세요.")
    @Size(max = 300, message = "제목은 300자 이하여야 합니다.")
    String title,
    @NotNull(message = "게시 시작일을 입력하세요.")
    LocalDate postFrom,
    @NotNull(message = "게시 종료일을 입력하세요.")
    LocalDate postTo,
    @Size(max = 200, message = "대상 역할은 200자 이하여야 합니다.")
    String targetRoles,
    @Size(max = 200, message = "대상 조직은 200자 이하여야 합니다.")
    String targetOrganizations,
    @NotNull(message = "중요 여부를 선택하세요.")
    Boolean important,
    @NotNull(message = "사용 여부를 선택하세요.")
    Boolean enabled,
    @NotBlank(message = "변경 사유를 입력하세요.")
    @Size(max = 500, message = "변경 사유는 500자 이하여야 합니다.")
    String reason
) {
    public Long noticeId() {
        return Long.valueOf(id.trim());
    }

    public String normalizedTitle() {
        return title.trim();
    }

    public String normalizedTargetRoles() {
        return blankToNull(targetRoles);
    }

    public String normalizedTargetOrganizations() {
        return blankToNull(targetOrganizations);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
