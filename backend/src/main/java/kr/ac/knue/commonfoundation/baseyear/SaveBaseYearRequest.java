package kr.ac.knue.commonfoundation.baseyear;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SaveBaseYearRequest(
    @NotBlank(message = "기준연도를 입력하세요.")
    @Pattern(regexp = "^[0-9]{4}$", message = "기준연도는 4자리 숫자여야 합니다.")
    String id,
    @NotBlank(message = "기본 조회연도를 입력하세요.")
    @Pattern(regexp = "^[0-9]{4}$", message = "기본 조회연도는 4자리 숫자여야 합니다.")
    String defaultQueryYear,
    @NotNull(message = "기준정보 복사 여부를 선택하세요.")
    Boolean copyBaselineEnabled,
    @NotNull(message = "초기화 여부를 선택하세요.")
    Boolean resetEnabled,
    @NotNull(message = "사용 여부를 선택하세요.")
    Boolean enabled,
    @NotBlank(message = "변경 사유를 입력하세요.")
    @Size(max = 500, message = "변경 사유는 500자 이하여야 합니다.")
    String reason
) {
    public String baseYear() {
        return id.trim();
    }
}
