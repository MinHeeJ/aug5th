package kr.ac.knue.commonfoundation.exceltemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SaveExcelTemplateRequest(
    @NotBlank(message = "업로드 양식 ID를 입력하세요.")
    @Pattern(regexp = "^[0-9]+$", message = "업로드 양식 ID는 숫자여야 합니다.")
    String id,
    @NotBlank(message = "업무영역을 입력하세요.")
    @Size(max = 80, message = "업무영역은 80자 이하여야 합니다.")
    String businessArea,
    @NotBlank(message = "버전을 입력하세요.")
    @Size(max = 30, message = "버전은 30자 이하여야 합니다.")
    String version,
    @NotEmpty(message = "필수 컬럼 정의를 한 개 이상 등록하세요.")
    List<Map<String, Object>> requiredColumns,
    @NotNull(message = "적용일자를 입력하세요.")
    LocalDate effectiveDate,
    @NotNull(message = "사용여부를 선택하세요.")
    Boolean enabled,
    @NotBlank(message = "처리 사유를 입력하세요.")
    @Size(max = 500, message = "처리 사유는 500자 이하여야 합니다.")
    String reason
) {
    public Long templateId() {
        return Long.valueOf(id.trim());
    }

    public String normalizedBusinessArea() {
        return businessArea.trim();
    }

    public String normalizedVersion() {
        return version.trim();
    }
}
