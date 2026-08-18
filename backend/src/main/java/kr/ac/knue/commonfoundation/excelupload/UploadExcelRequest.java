package kr.ac.knue.commonfoundation.excelupload;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UploadExcelRequest(
    @NotBlank(message = "업로드 양식 ID를 입력하세요.")
    @Pattern(regexp = "^[0-9]+$", message = "업로드 양식 ID는 숫자여야 합니다.")
    String id,
    @NotBlank(message = "업로드 파일명을 입력하세요.")
    @Size(max = 300, message = "업로드 파일명은 300자 이하여야 합니다.")
    String fileName,
    @NotEmpty(message = "검증할 엑셀 행을 한 개 이상 입력하세요.")
    List<Map<String, Object>> rows,
    @NotBlank(message = "처리 사유를 입력하세요.")
    @Size(max = 500, message = "처리 사유는 500자 이하여야 합니다.")
    String reason
) {
    public Long templateId() {
        return Long.valueOf(id.trim());
    }

    public String normalizedFileName() {
        return fileName.trim();
    }
}
