package kr.ac.knue.commonfoundation.systemconfig;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SaveSystemConfigurationRequest(
    @NotBlank(message = "환경설정 키를 입력하세요.")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "환경설정 키는 영문 대문자, 숫자, _, -만 사용할 수 있습니다.")
    @Size(max = 80, message = "환경설정 키는 80자 이하여야 합니다.")
    String id,
    @NotBlank(message = "설정값을 입력하세요.")
    @Size(max = 300, message = "설정값은 300자 이하여야 합니다.")
    String configValue,
    @NotNull(message = "사용 여부를 선택하세요.")
    Boolean enabled,
    @NotBlank(message = "변경 사유를 입력하세요.")
    @Size(max = 500, message = "변경 사유는 500자 이하여야 합니다.")
    String reason
) {
    public String configKey() {
        return id.trim();
    }
}
