package kr.ac.knue.commonfoundation.excelupload;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

@WebMvcTest(controllers = ExcelUploadManagementController.class)
@Import({GlobalApiExceptionHandler.class, CurrentUserContext.class, AuthInterceptor.class, WebMvcConfig.class})
class SCREXCELUPLOADContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private ExcelUploadManagementService excelUploadManagementService;

    @Test
    void openApiFixtureContainsAuthoritativeUploadExcelContract() throws Exception {
        ClassPathResource resource = new ClassPathResource("contracts/openapi.yaml");
        String contract = new String(resource.getInputStream().readAllBytes());

        org.assertj.core.api.Assertions.assertThat(contract)
            .contains("/api/admin/excel-uploads")
            .contains("operationId: uploadExcel")
            .contains("x-roles:")
            .contains("- R09")
            .contains("REQ-066")
            .contains("REQ-067")
            .contains("REQ-068")
            .contains("REQ-069")
            .contains("REQ-070")
            .contains("REQ-071")
            .contains("REQ-154")
            .contains("REQ-210");
    }

    @Test
    void listExcelUploadsReturnsUploadHistoriesForR09() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(excelUploadManagementService.listExcelUploads(any(ExcelUploadSearchCondition.class)))
            .thenReturn(new ExcelUploadListResponse(List.of(new ExcelUploadListItem(
                1L,
                1L,
                "ACHIEVEMENT",
                "교수업적",
                "2026.1",
                "admin",
                "성과업로드_정상.xlsx",
                3,
                3,
                0,
                0,
                3,
                1250,
                "SUCCESS",
                LocalDateTime.of(2026, 8, 16, 13, 0),
                "모든 행이 정상일 때만 하나의 트랜잭션으로 등록합니다.",
                "업무별 확정 양식 버전과 헤더·필수값·형식·코드·중복을 검증합니다."
            )), 1, 20, 1, "SCR-EXCEL-UPLOAD", "R09"));

        mockMvc.perform(get("/api/admin/excel-uploads")
                .param("page", "1")
                .param("size", "20")
                .param("q", "성과")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.screenId").value("SCR-EXCEL-UPLOAD"))
            .andExpect(jsonPath("$.data.requiredRole").value("R09"))
            .andExpect(jsonPath("$.data.totalCount").value(1))
            .andExpect(jsonPath("$.data.items[0].uploadId").value(1))
            .andExpect(jsonPath("$.data.items[0].businessArea").value("ACHIEVEMENT"))
            .andExpect(jsonPath("$.data.items[0].successCount").value(3))
            .andExpect(jsonPath("$.data.items[0].errorCount").value(0))
            .andExpect(jsonPath("$.data.items[0].uploadStatus").value("SUCCESS"));
    }

    @Test
    void uploadExcelCreatesHistoryAndReturnsTransactionSummary() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(excelUploadManagementService.uploadExcel(any(UploadExcelRequest.class)))
            .thenReturn(new UploadExcelResponse(7L, 1L, "SUCCESS", 2, 2, 0, 0, 2, "엑셀 업로드 검증과 등록이 완료되었습니다."));

        mockMvc.perform(post("/api/admin/excel-uploads")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"1\",\"fileName\":\"성과업로드_정상.xlsx\",\"rows\":[{\"교번\":\"T-1001\",\"업적구분\":\"논문\",\"점수\":\"10\"},{\"교번\":\"T-1002\",\"업적구분\":\"저서\",\"점수\":\"8\"}],\"reason\":\"엑셀 업로드 화면 저장 CTA\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.uploadId").value(7))
            .andExpect(jsonPath("$.data.uploadStatus").value("SUCCESS"))
            .andExpect(jsonPath("$.data.totalCount").value(2))
            .andExpect(jsonPath("$.data.savedCount").value(2))
            .andExpect(jsonPath("$.data.message").value("엑셀 업로드 검증과 등록이 완료되었습니다."));

        verify(excelUploadManagementService).uploadExcel(any(UploadExcelRequest.class));
    }

    @Test
    void uploadExcelMissingTemplateIdentifierReturnsFieldValidationError() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));

        mockMvc.perform(post("/api/admin/excel-uploads")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fileName\":\"성과업로드_오류.xlsx\",\"rows\":[],\"reason\":\"필수값 검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.fields.id").exists());
    }

    @Test
    void listExcelUploadsForNonR09ReturnsForbiddenEnvelope() throws Exception {
        when(authService.authenticate("R01-SESSION"))
            .thenReturn(new SessionPrincipal("R01-SESSION", new AuthenticatedUser("teacher01", List.of("R01"), "SELF")));

        mockMvc.perform(get("/api/admin/excel-uploads")
                .cookie(new Cookie("KNUE_SESSION_ID", "R01-SESSION")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    void downloadExcelUploadErrorsReturnsValidatedErrorFileMetadata() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(excelUploadManagementService.downloadExcelUploadErrors(eq(7L)))
            .thenReturn(new ExcelUploadErrorDownloadResponse(7L, "성과업로드_오류_결과.xlsx", 3, "오류 행 다운로드 준비가 완료되었습니다.", "오류 건이 존재하는 업로드 이력만 다운로드합니다."));

        mockMvc.perform(get("/api/admin/excel-uploads/7/errors/download")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.uploadId").value(7))
            .andExpect(jsonPath("$.data.fileName").value("성과업로드_오류_결과.xlsx"))
            .andExpect(jsonPath("$.data.errorCount").value(3))
            .andExpect(jsonPath("$.data.validationRule").value("오류 건이 존재하는 업로드 이력만 다운로드합니다."));

        verify(excelUploadManagementService).downloadExcelUploadErrors(eq(7L));
    }
}
