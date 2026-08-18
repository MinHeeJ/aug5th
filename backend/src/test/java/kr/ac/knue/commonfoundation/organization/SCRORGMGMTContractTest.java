package kr.ac.knue.commonfoundation.organization;

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

@WebMvcTest(controllers = OrganizationManagementController.class)
@Import({GlobalApiExceptionHandler.class, CurrentUserContext.class, AuthInterceptor.class, WebMvcConfig.class})
class SCRORGMGMTContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private OrganizationManagementService organizationManagementService;

    @Test
    void openApiFixtureContainsAuthoritativeListOrganizationsContract() throws Exception {
        ClassPathResource resource = new ClassPathResource("contracts/openapi.yaml");
        String contract = new String(resource.getInputStream().readAllBytes());

        org.assertj.core.api.Assertions.assertThat(contract)
            .contains("operationId: listOrganizations")
            .contains("/api/admin/organizations")
            .contains("x-roles:")
            .contains("- R09");
    }

    @Test
    void listOrganizationsReturnsHierarchyAndPagingEnvelopeForR09() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(organizationManagementService.listOrganizations(any(OrganizationSearchCondition.class)))
            .thenReturn(new OrganizationListResponse(List.of(new OrganizationListItem(
                "KNUE-EDU",
                "교육학과",
                "KNUE-COLLEGE",
                "사범대학",
                "2026-01-01",
                null,
                true,
                0,
                1
            )), 1, 20, 1, "SCR-ORG-MGMT", "R09"));

        mockMvc.perform(get("/api/admin/organizations")
                .param("page", "1")
                .param("size", "20")
                .param("q", "교육")
                .param("filter", "parentOrganizationCode=KNUE-COLLEGE;enabled=true")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.screenId").value("SCR-ORG-MGMT"))
            .andExpect(jsonPath("$.data.requiredRole").value("R09"))
            .andExpect(jsonPath("$.data.totalCount").value(1))
            .andExpect(jsonPath("$.data.items[0].organizationCode").value("KNUE-EDU"))
            .andExpect(jsonPath("$.data.items[0].organizationName").value("교육학과"))
            .andExpect(jsonPath("$.data.items[0].parentOrganizationCode").value("KNUE-COLLEGE"))
            .andExpect(jsonPath("$.data.items[0].parentOrganizationName").value("사범대학"))
            .andExpect(jsonPath("$.data.items[0].validFrom").value("2026-01-01"))
            .andExpect(jsonPath("$.data.items[0].enabled").value(true))
            .andExpect(jsonPath("$.data.items[0].assignedUserCount").value(1));
    }

    @Test
    void listOrganizationsWithoutSessionReturnsUnauthorizedEnvelope() throws Exception {
        when(authService.authenticate(null))
            .thenThrow(new kr.ac.knue.commonfoundation.api.ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));

        mockMvc.perform(get("/api/admin/organizations"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void listOrganizationsForNonR09ReturnsForbiddenEnvelope() throws Exception {
        when(authService.authenticate("R01-SESSION"))
            .thenReturn(new SessionPrincipal("R01-SESSION", new AuthenticatedUser("teacher01", List.of("R01"), "SELF")));

        mockMvc.perform(get("/api/admin/organizations")
                .cookie(new Cookie("KNUE_SESSION_ID", "R01-SESSION")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    void saveOrganizationUpdatesLocalManagementFieldsAndReturnsSuccessMessage() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(organizationManagementService.saveOrganization(any(SaveOrganizationRequest.class)))
            .thenReturn(new SaveOrganizationResponse("KNUE-EDU", false, "2026-12-31", "조직 관리 저장이 완료되었습니다."));

        mockMvc.perform(post("/api/admin/organizations")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"KNUE-EDU\",\"enabled\":false,\"validTo\":\"2026-12-31\",\"reason\":\"조직 개편 반영\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.organizationCode").value("KNUE-EDU"))
            .andExpect(jsonPath("$.data.enabled").value(false))
            .andExpect(jsonPath("$.data.validTo").value("2026-12-31"))
            .andExpect(jsonPath("$.data.message").value("조직 관리 저장이 완료되었습니다."));

        verify(organizationManagementService).saveOrganization(any(SaveOrganizationRequest.class));
    }

    @Test
    void saveOrganizationMissingIdentifierReturnsFieldValidationError() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));

        mockMvc.perform(post("/api/admin/organizations")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"enabled\":true,\"reason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.fields.id").exists());
    }
}
