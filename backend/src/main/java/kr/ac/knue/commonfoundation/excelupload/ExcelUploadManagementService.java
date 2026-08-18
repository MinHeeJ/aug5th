package kr.ac.knue.commonfoundation.excelupload;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.knue.commonfoundation.api.ApiException;
import kr.ac.knue.commonfoundation.auth.CurrentUserContext;
import kr.ac.knue.commonfoundation.auth.SessionPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExcelUploadManagementService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ExcelUploadMapper excelUploadMapper;
    private final CurrentUserContext currentUserContext;

    public ExcelUploadManagementService(ExcelUploadMapper excelUploadMapper, CurrentUserContext currentUserContext) {
        this.excelUploadMapper = excelUploadMapper;
        this.currentUserContext = currentUserContext;
    }

    @Transactional(readOnly = true)
    public ExcelUploadListResponse listExcelUploads(ExcelUploadSearchCondition condition) {
        return new ExcelUploadListResponse(
            excelUploadMapper.selectExcelUploads(condition),
            condition.page(),
            condition.size(),
            excelUploadMapper.countExcelUploads(condition),
            "SCR-EXCEL-UPLOAD",
            "R09"
        );
    }

    @Transactional
    public UploadExcelResponse uploadExcel(UploadExcelRequest request) {
        long started = System.nanoTime();
        SessionPrincipal principal = requireCurrentUser();
        ExcelUploadTemplate template = excelUploadMapper.selectTemplate(request.templateId());
        validateTemplate(template);
        List<Map<String, Object>> columns = parseRequiredColumns(template.requiredColumns());
        List<ExcelUploadValidationError> errors = validateRows(request.rows(), columns);
        int totalCount = request.rows().size();
        int errorCount = errors.size();
        String uploadStatus = errorCount == 0 ? "SUCCESS" : "FAILED";
        int successCount = errorCount == 0 ? totalCount : 0;
        int excludedCount = errorCount == 0 ? 0 : totalCount;
        int savedCount = successCount;
        int processingTimeMs = (int) Math.max(1L, (System.nanoTime() - started) / 1_000_000L);

        GeneratedUploadId holder = new GeneratedUploadId();
        excelUploadMapper.insertUploadHistory(
            holder,
            template.templateId(),
            principal.user().userId(),
            request.normalizedFileName(),
            totalCount,
            successCount,
            errorCount,
            excludedCount,
            savedCount,
            processingTimeMs,
            uploadStatus
        );
        Long uploadId = holder.getUploadId();
        for (ExcelUploadValidationError error : errors) {
            excelUploadMapper.insertUploadError(uploadId, error);
        }
        excelUploadMapper.insertAudit(
            "EXCEL_UPLOAD",
            "excel_upload_histories:" + uploadId,
            principal.user().userId(),
            "{}",
            jsonValue(uploadId, template, request, totalCount, successCount, errorCount, excludedCount, savedCount, uploadStatus),
            uploadStatus.equals("SUCCESS") ? "SUCCESS" : "FAILED"
        );
        return new UploadExcelResponse(
            uploadId,
            template.templateId(),
            uploadStatus,
            totalCount,
            successCount,
            errorCount,
            excludedCount,
            savedCount,
            uploadStatus.equals("SUCCESS") ? "엑셀 업로드 검증과 등록이 완료되었습니다." : "엑셀 업로드 검증 오류가 있어 전체 행을 반영하지 않았습니다."
        );
    }

    @Transactional
    public ExcelUploadErrorDownloadResponse downloadExcelUploadErrors(long uploadId) {
        SessionPrincipal principal = requireCurrentUser();
        ExcelUploadListItem upload = excelUploadMapper.selectExcelUpload(uploadId);
        if (upload == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "엑셀 업로드 이력을 찾을 수 없습니다.");
        }
        long errorCount = excelUploadMapper.countUploadErrors(uploadId);
        excelUploadMapper.insertAudit(
            "READ",
            "excel_upload_histories:" + uploadId + ":errors-download",
            principal.user().userId(),
            "{}",
            "{\"uploadId\":" + uploadId + ",\"errorCount\":" + errorCount + "}",
            "SUCCESS"
        );
        return new ExcelUploadErrorDownloadResponse(
            upload.uploadId(),
            upload.fileName(),
            errorCount,
            "엑셀 오류목록 다운로드가 허용되었습니다.",
            upload.validationRule()
        );
    }

    private SessionPrincipal requireCurrentUser() {
        return currentUserContext.current()
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));
    }

    private static void validateTemplate(ExcelUploadTemplate template) {
        if (template == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "업로드 양식을 찾을 수 없습니다.");
        }
        if (!Boolean.TRUE.equals(template.enabled())) {
            throw new ApiException(HttpStatus.CONFLICT, "CONFLICT", "사용 중인 업로드 양식만 업로드할 수 있습니다.", Map.of("id", "사용 중인 양식을 선택하세요."));
        }
    }

    private static List<Map<String, Object>> parseRequiredColumns(String requiredColumns) {
        try {
            return OBJECT_MAPPER.readValue(requiredColumns, new TypeReference<>() {
            });
        } catch (JsonProcessingException exception) {
            throw new ApiException(HttpStatus.CONFLICT, "CONFLICT", "업로드 양식 컬럼 정의를 읽을 수 없습니다.", Map.of("id", "업로드 양식 컬럼 JSON을 확인하세요."));
        }
    }

    private static List<ExcelUploadValidationError> validateRows(List<Map<String, Object>> rows, List<Map<String, Object>> columns) {
        List<ExcelUploadValidationError> errors = new ArrayList<>();
        Map<String, List<String>> duplicateValues = new LinkedHashMap<>();
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Map<String, Object> row = rows.get(rowIndex);
            int rowNumber = rowIndex + 2;
            for (Map<String, Object> column : columns) {
                String name = String.valueOf(column.get("name"));
                String type = String.valueOf(column.get("type"));
                boolean required = Boolean.TRUE.equals(column.get("required"));
                boolean duplicateKey = Boolean.TRUE.equals(column.get("duplicateKey"));
                Object rawValue = row.get(name);
                String inputValue = rawValue == null ? null : String.valueOf(rawValue).trim();
                if (required && (inputValue == null || inputValue.isBlank())) {
                    errors.add(new ExcelUploadValidationError(rowNumber, name, inputValue, "REQUIRED", name + " 필수값을 입력하세요."));
                    continue;
                }
                if (inputValue != null && !inputValue.isBlank() && "NUMBER".equalsIgnoreCase(type) && !inputValue.matches("^-?[0-9]+(\\.[0-9]+)?$")) {
                    errors.add(new ExcelUploadValidationError(rowNumber, name, inputValue, "TYPE", name + " 값은 숫자 형식이어야 합니다."));
                }
                if (duplicateKey && inputValue != null && !inputValue.isBlank()) {
                    duplicateValues.computeIfAbsent(name + "\u0000" + inputValue, ignored -> new ArrayList<>()).add(String.valueOf(rowNumber));
                }
            }
        }
        for (Map.Entry<String, List<String>> entry : duplicateValues.entrySet()) {
            if (entry.getValue().size() > 1) {
                String[] key = entry.getKey().split("\u0000", 2);
                errors.add(new ExcelUploadValidationError(
                    Integer.parseInt(entry.getValue().get(0)),
                    key[0],
                    key.length > 1 ? key[1] : "",
                    "DUPLICATE",
                    "중복 검증 컬럼 " + key[0] + " 값이 행 " + String.join(",", entry.getValue()) + "에서 중복되었습니다."
                ));
            }
        }
        return errors;
    }

    private static String jsonValue(
        Long uploadId,
        ExcelUploadTemplate template,
        UploadExcelRequest request,
        int totalCount,
        int successCount,
        int errorCount,
        int excludedCount,
        int savedCount,
        String uploadStatus
    ) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("uploadId", uploadId);
        value.put("templateId", template.templateId());
        value.put("businessArea", template.businessArea());
        value.put("version", template.version());
        value.put("fileName", request.normalizedFileName());
        value.put("totalCount", totalCount);
        value.put("successCount", successCount);
        value.put("errorCount", errorCount);
        value.put("excludedCount", excludedCount);
        value.put("savedCount", savedCount);
        value.put("uploadStatus", uploadStatus);
        value.put("reason", request.reason());
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("감사 로그 JSON 생성에 실패했습니다.", exception);
        }
    }
}
