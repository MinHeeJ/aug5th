package kr.ac.knue.commonfoundation.exceldownload;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record CreateExcelDownloadRequest(
    @NotBlank(message = "업무영역 ID를 입력하세요.")
    @Size(max = 80, message = "업무영역 ID는 80자 이하여야 합니다.")
    String id,
    @NotEmpty(message = "현재 조회조건을 입력하세요.")
    Map<String, Object> queryCondition,
    @NotBlank(message = "처리 사유를 입력하세요.")
    @Size(max = 500, message = "처리 사유는 500자 이하여야 합니다.")
    String reason
) {
    public String businessArea() {
        return id.trim();
    }
}
