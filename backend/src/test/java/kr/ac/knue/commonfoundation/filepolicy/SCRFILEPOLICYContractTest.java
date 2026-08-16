package kr.ac.knue.commonfoundation.filepolicy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

@WebMvcTest(controllers = FilePolicyManagementController.class)
@Import({GlobalApiExceptionHandler.class, CurrentUserContext.class, AuthInterceptor.class, WebMvcConfig.class})
class SCRFILEPOLICYContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private FilePolicyManagementService filePolicyManagementService;

    @Test
    void openApiFixtureContainsAuthoritativeListFilePoliciesContract() throws Exception {
        ClassPathResource resource = new ClassPathResource("contracts/openapi.yaml");
        String contract = new String(resource.getInputStream().readAllBytes());

        org.assertj.core.api.Assertions.assertThat(contract)
            .contains("operationId: listFilePolicies")
            .contains("/api/admin/file-policies")
            .contains("x-roles:")
            .contains("- R09")
            .contains("REQ-049")
            .contains("REQ-050")
            .contains("REQ-051");
    }

    @Test
    void listFilePoliciesReturnsSourceBackedPolicyRowsForR09() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(filePolicyManagementService.listFilePolicies(any(FilePolicySearchCondition.class)))
            .thenReturn(new FilePolicyListResponse(List.of(new FilePolicyListItem(
                1L,
                "COMMON",
                "공통 첨부",
                "pdf,xlsx,docx,png,jpg",
                20,
                5,
                100,
                120,
                true,
                true,
                "첨부파일 업로드 검증 시 확장자·용량·개수·파일명 길이 정책을 적용합니다.",
                "이 화면에서는 실제 파일 업로드·조회·삭제를 수행하지 않습니다."
            )), 1, 20, 1, "SCR-FILE-POLICY", "R09"));

        mockMvc.perform(get("/api/admin/file-policies")
                .param("page", "1")
                .param("size", "20")
                .param("q", "COMMON")
                .param("filter", "businessArea=COMMON;malwareScanEnabled=true")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.screenId").value("SCR-FILE-POLICY"))
            .andExpect(jsonPath("$.data.requiredRole").value("R09"))
            .andExpect(jsonPath("$.data.totalCount").value(1))
            .andExpect(jsonPath("$.data.items[0].filePolicyId").value(1))
            .andExpect(jsonPath("$.data.items[0].businessArea").value("COMMON"))
            .andExpect(jsonPath("$.data.items[0].allowedExtensions").value("pdf,xlsx,docx,png,jpg"))
            .andExpect(jsonPath("$.data.items[0].maxFileSizeMb").value(20))
            .andExpect(jsonPath("$.data.items[0].maxFileCount").value(5))
            .andExpect(jsonPath("$.data.items[0].maxTotalSizeMb").value(100))
            .andExpect(jsonPath("$.data.items[0].maxFilenameLength").value(120))
            .andExpect(jsonPath("$.data.items[0].malwareScanEnabled").value(true));
    }

    @Test
    void listFilePoliciesWithoutSessionReturnsUnauthorizedEnvelope() throws Exception {
        when(authService.authenticate(null))
            .thenThrow(new kr.ac.knue.commonfoundation.api.ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));

        mockMvc.perform(get("/api/admin/file-policies"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void listFilePoliciesForNonR09ReturnsForbiddenEnvelope() throws Exception {
        when(authService.authenticate("R01-SESSION"))
            .thenReturn(new SessionPrincipal("R01-SESSION", new AuthenticatedUser("teacher01", List.of("R01"), "SELF")));

        mockMvc.perform(get("/api/admin/file-policies")
                .cookie(new Cookie("KNUE_SESSION_ID", "R01-SESSION")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    void saveFilePolicyUpdatesValidationFieldsAndReturnsSuccessMessage() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(filePolicyManagementService.saveFilePolicy(any(SaveFilePolicyRequest.class)))
            .thenReturn(new SaveFilePolicyResponse(1L, "COMMON", "pdf,xlsx,docx,png,jpg", 25, 6, 120, 120, true, true, "파일정책 관리 저장이 완료되었습니다."));

        mockMvc.perform(post("/api/admin/file-policies")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"1\",\"allowedExtensions\":\"pdf,xlsx,docx,png,jpg\",\"maxFileSizeMb\":25,\"maxFileCount\":6,\"maxTotalSizeMb\":120,\"maxFilenameLength\":120,\"malwareScanEnabled\":true,\"enabled\":true,\"reason\":\"업무별 첨부 정책 조정\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.filePolicyId").value(1))
            .andExpect(jsonPath("$.data.maxFileSizeMb").value(25))
            .andExpect(jsonPath("$.data.maxFileCount").value(6))
            .andExpect(jsonPath("$.data.maxTotalSizeMb").value(120))
            .andExpect(jsonPath("$.data.message").value("파일정책 관리 저장이 완료되었습니다."));

        verify(filePolicyManagementService).saveFilePolicy(any(SaveFilePolicyRequest.class));
    }

    @Test
    void saveFilePolicyMissingIdentifierReturnsFieldValidationError() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));

        mockMvc.perform(post("/api/admin/file-policies")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"allowedExtensions\":\"pdf\",\"maxFileSizeMb\":10,\"maxFileCount\":1,\"maxTotalSizeMb\":10,\"maxFilenameLength\":80,\"malwareScanEnabled\":true,\"enabled\":true,\"reason\":\"필수값 검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.fields.id").exists());
    }
}
