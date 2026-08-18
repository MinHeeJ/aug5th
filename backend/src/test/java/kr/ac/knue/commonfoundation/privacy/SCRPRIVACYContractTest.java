package kr.ac.knue.commonfoundation.privacy;

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

@WebMvcTest(controllers = PrivacyPolicyManagementController.class)
@Import({GlobalApiExceptionHandler.class, CurrentUserContext.class, AuthInterceptor.class, WebMvcConfig.class})
class SCRPRIVACYContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private PrivacyPolicyManagementService privacyPolicyManagementService;

    @Test
    void openApiFixtureContainsAuthoritativeListPrivacyPoliciesContract() throws Exception {
        ClassPathResource resource = new ClassPathResource("contracts/openapi.yaml");
        String contract = new String(resource.getInputStream().readAllBytes());

        org.assertj.core.api.Assertions.assertThat(contract)
            .contains("/api/admin/privacy-policies")
            .contains("operationId: listPrivacyPolicies")
            .contains("operationId: savePrivacyPolicies")
            .contains("x-roles:")
            .contains("- R09")
            .contains("REQ-076")
            .contains("REQ-077")
            .contains("REQ-078")
            .contains("REQ-079")
            .contains("REQ-080")
            .contains("REQ-081")
            .contains("privacy_field_policies");
    }

    @Test
    void listPrivacyPoliciesReturnsMaskedEncryptionPolicyCatalogForR09() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(privacyPolicyManagementService.listPrivacyPolicies(any(PrivacyPolicySearchCondition.class)))
            .thenReturn(new PrivacyPolicyListResponse(List.of(new PrivacyPolicyListItem(
                1L,
                "researcher_registration_no",
                "SENSITIVE",
                "민감정보",
                true,
                "앞 3자리 + 뒤 2자리 표시",
                true,
                "AES-256-GCM 암호화와 HMAC 검색 식별자 적용 대상입니다.",
                "감사로그에는 원문과 처리값을 제외하고 목적·결과만 기록합니다."
            )), 1, 20, 1, "SCR-PRIVACY", "R09"));

        mockMvc.perform(get("/api/admin/privacy-policies")
                .param("page", "1")
                .param("size", "20")
                .param("q", "연구자")
                .param("filter", "privacyGrade=SENSITIVE;encryptionEnabled=true;logExcluded=true")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.screenId").value("SCR-PRIVACY"))
            .andExpect(jsonPath("$.data.requiredRole").value("R09"))
            .andExpect(jsonPath("$.data.totalCount").value(1))
            .andExpect(jsonPath("$.data.items[0].fieldPolicyId").value(1))
            .andExpect(jsonPath("$.data.items[0].fieldName").value("researcher_registration_no"))
            .andExpect(jsonPath("$.data.items[0].privacyGrade").value("SENSITIVE"))
            .andExpect(jsonPath("$.data.items[0].encryptionEnabled").value(true))
            .andExpect(jsonPath("$.data.items[0].logExcluded").value(true));
    }

    @Test
    void savePrivacyPolicyUpdatesPolicyAndReturnsAuditSafeSummary() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(privacyPolicyManagementService.savePrivacyPolicy(any(SavePrivacyPolicyRequest.class)))
            .thenReturn(new SavePrivacyPolicyResponse(1L, "phone_number", "PERSONAL", true, true, "개인정보 관리 정책 저장이 완료되었습니다."));

        mockMvc.perform(post("/api/admin/privacy-policies")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"1\",\"fieldName\":\"phone_number\",\"privacyGrade\":\"PERSONAL\",\"encryptionEnabled\":true,\"maskingRule\":\"뒤 4자리 마스킹\",\"logExcluded\":true,\"reason\":\"개인정보 관리 화면 저장 CTA\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.fieldPolicyId").value(1))
            .andExpect(jsonPath("$.data.fieldName").value("phone_number"))
            .andExpect(jsonPath("$.data.privacyGrade").value("PERSONAL"))
            .andExpect(jsonPath("$.data.encryptionEnabled").value(true))
            .andExpect(jsonPath("$.data.logExcluded").value(true));

        verify(privacyPolicyManagementService).savePrivacyPolicy(any(SavePrivacyPolicyRequest.class));
    }

    @Test
    void savePrivacyPolicyMissingIdentifierReturnsFieldValidationError() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));

        mockMvc.perform(post("/api/admin/privacy-policies")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fieldName\":\"phone_number\",\"privacyGrade\":\"PERSONAL\",\"encryptionEnabled\":true,\"maskingRule\":\"뒤 4자리 마스킹\",\"logExcluded\":true,\"reason\":\"필수값 검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.fields.id").exists());
    }

    @Test
    void savePrivacyPolicyForNonR09ReturnsForbiddenEnvelope() throws Exception {
        when(authService.authenticate("R01-SESSION"))
            .thenReturn(new SessionPrincipal("R01-SESSION", new AuthenticatedUser("teacher01", List.of("R01"), "SELF")));

        mockMvc.perform(post("/api/admin/privacy-policies")
                .cookie(new Cookie("KNUE_SESSION_ID", "R01-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"1\",\"fieldName\":\"phone_number\",\"privacyGrade\":\"PERSONAL\",\"encryptionEnabled\":true,\"maskingRule\":\"뒤 4자리 마스킹\",\"logExcluded\":true,\"reason\":\"권한 검증\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }
}
