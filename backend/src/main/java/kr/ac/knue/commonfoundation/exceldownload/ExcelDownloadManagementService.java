package kr.ac.knue.commonfoundation.exceldownload;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
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
public class ExcelDownloadManagementService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ExcelDownloadMapper excelDownloadMapper;
    private final CurrentUserContext currentUserContext;

    public ExcelDownloadManagementService(ExcelDownloadMapper excelDownloadMapper, CurrentUserContext currentUserContext) {
        this.excelDownloadMapper = excelDownloadMapper;
        this.currentUserContext = currentUserContext;
    }

    @Transactional(readOnly = true)
    public ExcelDownloadListResponse listExcelDownloads(ExcelDownloadSearchCondition condition) {
        return new ExcelDownloadListResponse(
            excelDownloadMapper.selectExcelDownloads(condition),
            condition.page(),
            condition.size(),
            excelDownloadMapper.countExcelDownloads(condition),
            "SCR-EXCEL-DOWNLOAD",
            "R09"
        );
    }

    @Transactional
    public CreateExcelDownloadResponse createExcelDownload(CreateExcelDownloadRequest request) {
        SessionPrincipal principal = currentUserContext.current()
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));
        Map<String, Object> queryCondition = new LinkedHashMap<>(request.queryCondition());
        queryCondition.put("businessArea", request.businessArea());
        queryCondition.put("requestedBy", principal.user().userId());
        Map<String, Object> dataScope = new LinkedHashMap<>();
        dataScope.put("role", principal.user().roles().contains("R09") ? "R09" : String.join(",", principal.user().roles()));
        dataScope.put("scope", principal.user().dataScope());
        dataScope.put("serverEnforced", true);

        String fileName = request.businessArea() + "_조회결과_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".xlsx";
        String storedName = "excel-downloads/" + principal.user().userId() + "/" + fileName;
        long estimatedSize = Math.max(1024L, queryCondition.toString().length() * 128L);

        GeneratedDownloadId fileHolder = new GeneratedDownloadId();
        excelDownloadMapper.insertGeneratedFile(fileHolder, "EXCEL_DOWNLOAD:" + request.businessArea(), fileName, storedName, estimatedSize, principal.user().userId());
        Long fileId = fileHolder.getDownloadId();

        GeneratedDownloadId requestHolder = new GeneratedDownloadId();
        excelDownloadMapper.insertDownloadRequest(
            requestHolder,
            principal.user().userId(),
            json(queryCondition),
            json(dataScope),
            fileId
        );
        Long downloadId = requestHolder.getDownloadId();
        excelDownloadMapper.insertAudit(
            "EXCEL_DOWNLOAD",
            "excel_download_requests:" + downloadId,
            principal.user().userId(),
            "{}",
            jsonValue(downloadId, fileId, fileName, queryCondition, dataScope, request.reason()),
            "SUCCESS"
        );
        return new CreateExcelDownloadResponse(downloadId, fileId, fileName, "READY", "엑셀 다운로드 요청이 생성되었습니다.");
    }

    private static String json(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("엑셀 다운로드 JSON 생성에 실패했습니다.", exception);
        }
    }

    private static String jsonValue(Long downloadId, Long fileId, String fileName, Map<String, Object> queryCondition, Map<String, Object> dataScope, String reason) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("downloadId", downloadId);
        value.put("fileId", fileId);
        value.put("fileName", fileName);
        value.put("queryCondition", queryCondition);
        value.put("dataScopeApplied", dataScope);
        value.put("reason", reason);
        return json(value);
    }
}
