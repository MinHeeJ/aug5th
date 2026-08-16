package kr.ac.knue.commonfoundation.filepolicy;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SaveFilePolicyRequest(
    @NotBlank(message = "파일정책 ID를 입력하세요.")
    @Pattern(regexp = "^[0-9]+$", message = "파일정책 ID는 숫자여야 합니다.")
    String id,
    @NotBlank(message = "허용 확장자를 입력하세요.")
    @Size(max = 200, message = "허용 확장자는 200자 이하여야 합니다.")
    @Pattern(regexp = "^[a-z0-9]+(,[a-z0-9]+)*$", message = "허용 확장자는 쉼표로 구분한 소문자 확장자여야 합니다.")
    String allowedExtensions,
    @NotNull(message = "단일 파일 최대용량을 입력하세요.")
    @Min(value = 1, message = "단일 파일 최대용량은 1MB 이상이어야 합니다.")
    @Max(value = 1024, message = "단일 파일 최대용량은 1024MB 이하여야 합니다.")
    Integer maxFileSizeMb,
    @NotNull(message = "건당 첨부개수를 입력하세요.")
    @Min(value = 1, message = "건당 첨부개수는 1개 이상이어야 합니다.")
    @Max(value = 100, message = "건당 첨부개수는 100개 이하여야 합니다.")
    Integer maxFileCount,
    @NotNull(message = "전체용량을 입력하세요.")
    @Min(value = 1, message = "전체용량은 1MB 이상이어야 합니다.")
    @Max(value = 10240, message = "전체용량은 10240MB 이하여야 합니다.")
    Integer maxTotalSizeMb,
    @NotNull(message = "파일명 길이를 입력하세요.")
    @Min(value = 10, message = "파일명 길이는 10자 이상이어야 합니다.")
    @Max(value = 255, message = "파일명 길이는 255자 이하여야 합니다.")
    Integer maxFilenameLength,
    @NotNull(message = "악성파일 검사 적용여부를 선택하세요.")
    Boolean malwareScanEnabled,
    @NotNull(message = "사용 여부를 선택하세요.")
    Boolean enabled,
    @NotBlank(message = "변경 사유를 입력하세요.")
    @Size(max = 500, message = "변경 사유는 500자 이하여야 합니다.")
    String reason
) {
    public Long filePolicyId() {
        return Long.valueOf(id.trim());
    }

    public String normalizedAllowedExtensions() {
        return allowedExtensions.trim();
    }
}
