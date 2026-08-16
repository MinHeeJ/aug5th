package kr.ac.knue.commonfoundation.exceltemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
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

@WebMvcTest(controllers = ExcelTemplateManagementController.class)
@Import({GlobalApiExceptionHandler.class, CurrentUserContext.class, AuthInterceptor.class, WebMvcConfig.class})
class SCREXCELTEMPLATEContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private ExcelTemplateManagementService excelTemplateManagementService;

    @Test
    void openApiFixtureContainsAuthoritativeListExcelTemplatesContract() throws Exception {
        ClassPathResource resource = new ClassPathResource("contracts/openapi.yaml");
        String contract = new String(resource.getInputStream().readAllBytes());

        org.assertj.core.api.Assertions.assertThat(contract)
            .contains("operationId: listExcelTemplates")
            .contains("/api/admin/excel-templates")
            .contains("operationId: saveExcelTemplates")
            .contains("x-roles:")
            .contains("- R09")
            .contains("REQ-063")
            .contains("REQ-064")
            .contains("REQ-065")
            .contains("REQ-153")
            .contains("REQ-210");
    }

    @Test
    void listExcelTemplatesReturnsTemplateRegistryRowsForR09() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(excelTemplateManagementService.listExcelTemplates(any(ExcelTemplateSearchCondition.class)))
            .thenReturn(new ExcelTemplateListResponse(List.of(new ExcelTemplateListItem(
                1L,
                "ACHIEVEMENT",
                "교수업적",
                "2026.1",
                "[{\"name\":\"교번\",\"type\":\"STRING\",\"required\":true}]",
                1,
                LocalDate.of(2026, 3, 1),
                1L,
                "교수업적_업로드양식_2026.xlsx",
                true,
                "필수값·타입·중복규칙을 템플릿 버전으로 검증합니다.",
                "다운로드 시 첨부파일 권한과 템플릿 사용여부를 재검증합니다.",
                LocalDateTime.of(2026, 8, 16, 12, 0)
            )), 1, 20, 1, "SCR-EXCEL-TEMPLATE", "R09"));

        mockMvc.perform(get("/api/admin/excel-templates")
                .param("page", "1")
                .param("size", "20")
                .param("q", "업적")
                .param("filter", "businessArea=ACHIEVEMENT;enabled=true")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.screenId").value("SCR-EXCEL-TEMPLATE"))
            .andExpect(jsonPath("$.data.requiredRole").value("R09"))
            .andExpect(jsonPath("$.data.totalCount").value(1))
            .andExpect(jsonPath("$.data.items[0].templateId").value(1))
            .andExpect(jsonPath("$.data.items[0].businessArea").value("ACHIEVEMENT"))
            .andExpect(jsonPath("$.data.items[0].version").value("2026.1"))
            .andExpect(jsonPath("$.data.items[0].requiredColumnCount").value(1))
            .andExpect(jsonPath("$.data.items[0].downloadFileName").value("교수업적_업로드양식_2026.xlsx"));
    }

    @Test
    void listExcelTemplatesWithoutSessionReturnsUnauthorizedEnvelope() throws Exception {
        when(authService.authenticate(null))
            .thenThrow(new kr.ac.knue.commonfoundation.api.ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));

        mockMvc.perform(get("/api/admin/excel-templates"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void listExcelTemplatesForNonR09ReturnsForbiddenEnvelope() throws Exception {
        when(authService.authenticate("R01-SESSION"))
            .thenReturn(new SessionPrincipal("R01-SESSION", new AuthenticatedUser("teacher01", List.of("R01"), "SELF")));

        mockMvc.perform(get("/api/admin/excel-templates")
                .cookie(new Cookie("KNUE_SESSION_ID", "R01-SESSION")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    void saveExcelTemplateReturnsSuccessMessage() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(excelTemplateManagementService.saveExcelTemplate(any(SaveExcelTemplateRequest.class)))
            .thenReturn(new SaveExcelTemplateResponse(1L, "ACHIEVEMENT", "2026.1", true, 2, "업로드 양식 관리 저장이 완료되었습니다."));

        mockMvc.perform(post("/api/admin/excel-templates")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"1\",\"businessArea\":\"ACHIEVEMENT\",\"version\":\"2026.1\",\"requiredColumns\":[{\"name\":\"교번\",\"type\":\"STRING\",\"required\":true},{\"name\":\"점수\",\"type\":\"NUMBER\",\"required\":true}],\"effectiveDate\":\"2026-03-01\",\"enabled\":true,\"reason\":\"업로드 양식 검증 규칙 갱신\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.templateId").value(1))
            .andExpect(jsonPath("$.data.businessArea").value("ACHIEVEMENT"))
            .andExpect(jsonPath("$.data.requiredColumnCount").value(2))
            .andExpect(jsonPath("$.data.message").value("업로드 양식 관리 저장이 완료되었습니다."));

        verify(excelTemplateManagementService).saveExcelTemplate(any(SaveExcelTemplateRequest.class));
    }

    @Test
    void saveExcelTemplateMissingIdentifierReturnsFieldValidationError() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));

        mockMvc.perform(post("/api/admin/excel-templates")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"businessArea\":\"ACHIEVEMENT\",\"version\":\"2026.1\",\"requiredColumns\":[],\"effectiveDate\":\"2026-03-01\",\"enabled\":true,\"reason\":\"필수값 검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.fields.id").exists());
    }

    @Test
    void downloadExcelTemplateReturnsAttachmentReferenceAndRule() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(excelTemplateManagementService.downloadExcelTemplate(eq(1L)))
            .thenReturn(new ExcelTemplateDownloadResponse(1L, 10L, "교수업적_업로드양식_2026.xlsx", "업로드 양식 다운로드 준비가 완료되었습니다.", "템플릿 사용여부와 첨부파일 권한을 재검증합니다."));

        mockMvc.perform(get("/api/admin/excel-templates/1/download")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.templateId").value(1))
            .andExpect(jsonPath("$.data.fileId").value(10))
            .andExpect(jsonPath("$.data.fileName").value("교수업적_업로드양식_2026.xlsx"))
            .andExpect(jsonPath("$.data.downloadRule").value("템플릿 사용여부와 첨부파일 권한을 재검증합니다."));

        verify(excelTemplateManagementService).downloadExcelTemplate(eq(1L));
    }
}
