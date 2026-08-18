package kr.ac.knue.commonfoundation.codedetail;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SaveCodeDetailRequest(
    @NotBlank(message = "상세코드 ID를 입력하세요.")
    @Pattern(regexp = "^[0-9]+$", message = "상세코드 ID는 숫자여야 합니다.")
    String id,
    @NotBlank(message = "상세코드명을 입력하세요.")
    @Size(max = 200, message = "상세코드명은 200자 이하여야 합니다.")
    String codeName,
    @Size(max = 80, message = "상위 코드값은 80자 이하여야 합니다.")
    @Pattern(regexp = "^$|^[A-Z0-9_\\-]+$", message = "상위 코드값은 영문 대문자, 숫자, _, -만 사용할 수 있습니다.")
    String parentCodeValue,
    @NotNull(message = "정렬순서를 입력하세요.")
    @Min(value = 0, message = "정렬순서는 0 이상이어야 합니다.")
    @Max(value = 9999, message = "정렬순서는 9999 이하여야 합니다.")
    Integer displayOrder,
    @NotBlank(message = "변경 사유를 입력하세요.")
    @Size(max = 500, message = "변경 사유는 500자 이하여야 합니다.")
    String reason
) {
    public long codeDetailId() {
        return Long.parseLong(id);
    }
}
