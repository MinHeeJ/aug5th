package kr.ac.knue.commonfoundation.exceltemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.knue.commonfoundation.api.ApiException;
import kr.ac.knue.commonfoundation.auth.CurrentUserContext;
import kr.ac.knue.commonfoundation.auth.SessionPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExcelTemplateManagementService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ExcelTemplateMapper excelTemplateMapper;
    private final CurrentUserContext currentUserContext;

    public ExcelTemplateManagementService(ExcelTemplateMapper excelTemplateMapper, CurrentUserContext currentUserContext) {
        this.excelTemplateMapper = excelTemplateMapper;
        this.currentUserContext = currentUserContext;
    }

    @Transactional(readOnly = true)
    public ExcelTemplateListResponse listExcelTemplates(ExcelTemplateSearchCondition condition) {
        return new ExcelTemplateListResponse(
            excelTemplateMapper.selectExcelTemplates(condition),
            condition.page(),
            condition.size(),
            excelTemplateMapper.countExcelTemplates(condition),
            "SCR-EXCEL-TEMPLATE",
            "R09"
        );
    }

    @Transactional
    public SaveExcelTemplateResponse saveExcelTemplate(SaveExcelTemplateRequest request) {
        SessionPrincipal principal = requireCurrentUser();
        Long templateId = request.templateId();
        if (!excelTemplateMapper.existsExcelTemplate(templateId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "업로드 양식을 찾을 수 없습니다.");
        }
        ExcelTemplateListItem before = excelTemplateMapper.selectExcelTemplate(templateId);
        String requiredColumnsJson = canonicalRequiredColumns(request.requiredColumns());
        validateExcelTemplate(request, before, requiredColumnsJson);
        excelTemplateMapper.updateExcelTemplate(
            templateId,
            requiredColumnsJson,
            request.effectiveDate(),
            request.enabled(),
            principal.user().userId()
        );
        ExcelTemplateListItem saved = excelTemplateMapper.selectExcelTemplate(templateId);
        excelTemplateMapper.insertAudit(
            "UPDATE",
            "excel_templates:" + templateId,
            principal.user().userId(),
            jsonValue(before, null),
            jsonValue(saved, request.reason())
        );
        return new SaveExcelTemplateResponse(
            saved.templateId(),
            saved.businessArea(),
            saved.version(),
            saved.enabled(),
            saved.requiredColumnCount(),
            "업로드 양식 관리 저장이 완료되었습니다."
        );
    }

    @Transactional
    public ExcelTemplateDownloadResponse downloadExcelTemplate(long templateId) {
        SessionPrincipal principal = requireCurrentUser();
        if (!excelTemplateMapper.existsExcelTemplate(templateId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "업로드 양식을 찾을 수 없습니다.");
        }
        ExcelTemplateListItem template = excelTemplateMapper.selectExcelTemplate(templateId);
        if (!template.enabled() || template.downloadFileId() == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "다운로드 가능한 업로드 양식 파일이 없습니다.");
        }
        excelTemplateMapper.insertAudit(
            "READ",
            "excel_templates:" + templateId + ":download",
            principal.user().userId(),
            "{}",
            jsonValue(template, "DOWNLOAD")
        );
        return new ExcelTemplateDownloadResponse(
            template.templateId(),
            template.downloadFileId(),
            template.downloadFileName(),
            "업로드 양식 다운로드가 허용되었습니다.",
            template.downloadRule()
        );
    }

    private SessionPrincipal requireCurrentUser() {
        return currentUserContext.current()
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));
    }

    private void validateExcelTemplate(SaveExcelTemplateRequest request, ExcelTemplateListItem before, String requiredColumnsJson) {
        Map<String, String> fields = new LinkedHashMap<>();
        if (!before.businessArea().equals(request.normalizedBusinessArea())) {
            fields.put("businessArea", "업무영역은 템플릿 생명주기 식별자이므로 변경할 수 없습니다.");
        }
        if (!before.version().equals(request.normalizedVersion())) {
            fields.put("version", "버전은 템플릿 생명주기 식별자이므로 변경할 수 없습니다.");
        }
        if (excelTemplateMapper.existsDuplicateVersion(request.templateId(), request.normalizedBusinessArea(), request.normalizedVersion())) {
            fields.put("version", "같은 업무영역의 동일 버전 템플릿이 이미 존재합니다.");
        }
        if (!requiredColumnsJson.startsWith("[")) {
            fields.put("requiredColumns", "필수 컬럼 정의는 배열이어야 합니다.");
        }
        if (!fields.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "업로드 양식 처리 조건을 확인하세요.", fields);
        }
    }

    private static String canonicalRequiredColumns(List<Map<String, Object>> requiredColumns) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (int index = 0; index < requiredColumns.size(); index++) {
            Map<String, Object> column = requiredColumns.get(index);
            Object name = column.get("name");
            Object type = column.get("type");
            Object required = column.get("required");
            if (!(name instanceof String value) || value.isBlank()) {
                fields.put("requiredColumns", "필수 컬럼 " + (index + 1) + "번째 이름을 입력하세요.");
            }
            if (!(type instanceof String value) || value.isBlank()) {
                fields.put("requiredColumns", "필수 컬럼 " + (index + 1) + "번째 타입을 입력하세요.");
            }
            if (!(required instanceof Boolean)) {
                fields.put("requiredColumns", "필수 컬럼 " + (index + 1) + "번째 required 값을 true/false로 입력하세요.");
            }
        }
        if (!fields.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "업로드 양식 컬럼 정의를 확인하세요.", fields);
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(requiredColumns);
        } catch (JsonProcessingException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "업로드 양식 컬럼 정의를 JSON으로 변환할 수 없습니다.", Map.of("requiredColumns", "JSON 형식을 확인하세요."));
        }
    }

    private static String jsonValue(ExcelTemplateListItem item, String reason) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("templateId", item.templateId());
        value.put("businessArea", item.businessArea());
        value.put("version", item.version());
        value.put("requiredColumns", item.requiredColumns());
        value.put("effectiveDate", item.effectiveDate().toString());
        value.put("enabled", item.enabled());
        if (reason != null) {
            value.put("reason", reason);
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("감사 로그 JSON 생성에 실패했습니다.", exception);
        }
    }

}
