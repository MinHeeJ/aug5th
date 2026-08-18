package kr.ac.knue.commonfoundation.exceldownload;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.servlet.http.Cookie;
import kr.ac.knue.commonfoundation.api.GlobalApiExceptionHandler;
import kr.ac.knue.commonfoundation.auth.AuthInterceptor;
import kr.ac.knue.commonfoundation.auth.AuthService;
import kr.ac.knue.commonfoundation.auth.AuthenticatedUser;
import kr.ac.knue.commonfoundation.auth.CurrentUserContext;
import kr.ac.knue.commonfoundation.auth.SessionPrincipal;
import kr.ac.knue.commonfoundation.config.WebMvcConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ExcelDownloadManagementController.class)
@Import({GlobalApiExceptionHandler.class, CurrentUserContext.class, AuthInterceptor.class, WebMvcConfig.class})
class SCREXCELDOWNLOADContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private ExcelDownloadManagementService excelDownloadManagementService;

    @Test
    void openApiFixtureContainsAuthoritativeCreateExcelDownloadContract() throws Exception {
        ClassPathResource resource = new ClassPathResource("contracts/openapi.yaml");
        String contract = new String(resource.getInputStream().readAllBytes());

        org.assertj.core.api.Assertions.assertThat(contract)
            .contains("/api/admin/excel-downloads")
            .contains("operationId: createExcelDownload")
            .contains("x-roles:")
            .contains("- R09")
            .contains("REQ-072")
            .contains("REQ-073")
            .contains("REQ-074")
            .contains("REQ-075")
            .contains("REQ-155");
    }

    @Test
    void listExcelDownloadsReturnsRequestsWithAppliedScopeForR09() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(excelDownloadManagementService.listExcelDownloads(any(ExcelDownloadSearchCondition.class)))
            .thenReturn(new ExcelDownloadListResponse(List.of(new ExcelDownloadListItem(
                1L,
                "admin",
                "{\"businessArea\":\"ACHIEVEMENT\",\"q\":\"성과\"}",
                "{\"role\":\"R09\",\"scope\":\"ALL\"}",
                1L,
                "교수업적_조회결과_2026.xlsx",
                "xlsx",
                2048L,
                LocalDateTime.of(2026, 8, 16, 14, 0),
                "현재 조회조건과 사용자 데이터범위 권한을 적용하여 생성합니다.",
                "원천 업무자료는 변경하지 않고 권한 밖 자료는 포함하지 않습니다."
            )), 1, 20, 1, "SCR-EXCEL-DOWNLOAD", "R09"));

        mockMvc.perform(get("/api/admin/excel-downloads")
                .param("page", "1")
                .param("size", "20")
                .param("q", "성과")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.screenId").value("SCR-EXCEL-DOWNLOAD"))
            .andExpect(jsonPath("$.data.requiredRole").value("R09"))
            .andExpect(jsonPath("$.data.totalCount").value(1))
            .andExpect(jsonPath("$.data.items[0].downloadId").value(1))
            .andExpect(jsonPath("$.data.items[0].requesterId").value("admin"))
            .andExpect(jsonPath("$.data.items[0].fileName").value("교수업적_조회결과_2026.xlsx"))
            .andExpect(jsonPath("$.data.items[0].generationRule").value("현재 조회조건과 사용자 데이터범위 권한을 적용하여 생성합니다."));
    }

    @Test
    void createExcelDownloadPersistsRequestAndReturnsFileSummary() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(excelDownloadManagementService.createExcelDownload(any(CreateExcelDownloadRequest.class)))
            .thenReturn(new CreateExcelDownloadResponse(7L, 1L, "교수업적_조회결과_2026.xlsx", "READY", "엑셀 다운로드 요청이 생성되었습니다."));

        mockMvc.perform(post("/api/admin/excel-downloads")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"ACHIEVEMENT\",\"queryCondition\":{\"q\":\"성과\",\"year\":\"2026\"},\"reason\":\"엑셀 다운로드 화면 저장 CTA\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.downloadId").value(7))
            .andExpect(jsonPath("$.data.fileId").value(1))
            .andExpect(jsonPath("$.data.fileName").value("교수업적_조회결과_2026.xlsx"))
            .andExpect(jsonPath("$.data.status").value("READY"));

        verify(excelDownloadManagementService).createExcelDownload(any(CreateExcelDownloadRequest.class));
    }

    @Test
    void createExcelDownloadMissingBusinessAreaReturnsFieldValidationError() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));

        mockMvc.perform(post("/api/admin/excel-downloads")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"queryCondition\":{\"q\":\"성과\"},\"reason\":\"필수값 검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.fields.id").exists());
    }

    @Test
    void createExcelDownloadForNonR09ReturnsForbiddenEnvelope() throws Exception {
        when(authService.authenticate("R01-SESSION"))
            .thenReturn(new SessionPrincipal("R01-SESSION", new AuthenticatedUser("teacher01", List.of("R01"), "SELF")));

        mockMvc.perform(post("/api/admin/excel-downloads")
                .cookie(new Cookie("KNUE_SESSION_ID", "R01-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"ACHIEVEMENT\",\"queryCondition\":{\"q\":\"성과\"},\"reason\":\"권한 검증\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }
}
