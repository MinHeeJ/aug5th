package kr.ac.knue.commonfoundation.role;

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

@WebMvcTest(controllers = RoleManagementController.class)
@Import({GlobalApiExceptionHandler.class, CurrentUserContext.class, AuthInterceptor.class, WebMvcConfig.class})
class SCRROLEMGMTContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private RoleManagementService roleManagementService;

    @Test
    void openApiFixtureContainsAuthoritativeListRolesContract() throws Exception {
        ClassPathResource resource = new ClassPathResource("contracts/openapi.yaml");
        String contract = new String(resource.getInputStream().readAllBytes());

        org.assertj.core.api.Assertions.assertThat(contract)
            .contains("operationId: listRoles")
            .contains("/api/admin/roles")
            .contains("x-roles:")
            .contains("- R09");
    }

    @Test
    void listRolesReturnsRoleAndPermissionCountsForR09() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(roleManagementService.listRoles(any(RoleSearchCondition.class)))
            .thenReturn(new RoleListResponse(List.of(new RoleListItem(
                "R09",
                "시스템관리자",
                "사용자·조직·메뉴·권한 관리",
                "시스템 관리자 승인 대상자",
                "ALL",
                true,
                1,
                25,
                125
            )), 1, 20, 1, "SCR-ROLE-MGMT", "R09"));

        mockMvc.perform(get("/api/admin/roles")
                .param("page", "1")
                .param("size", "20")
                .param("q", "시스템")
                .param("filter", "enabled=true;defaultDataScope=ALL")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.screenId").value("SCR-ROLE-MGMT"))
            .andExpect(jsonPath("$.data.requiredRole").value("R09"))
            .andExpect(jsonPath("$.data.totalCount").value(1))
            .andExpect(jsonPath("$.data.items[0].roleCode").value("R09"))
            .andExpect(jsonPath("$.data.items[0].roleName").value("시스템관리자"))
            .andExpect(jsonPath("$.data.items[0].defaultDataScope").value("ALL"))
            .andExpect(jsonPath("$.data.items[0].enabled").value(true))
            .andExpect(jsonPath("$.data.items[0].assignedUserCount").value(1))
            .andExpect(jsonPath("$.data.items[0].menuPermissionCount").value(25))
            .andExpect(jsonPath("$.data.items[0].functionPermissionCount").value(125));
    }

    @Test
    void listRolesWithoutSessionReturnsUnauthorizedEnvelope() throws Exception {
        when(authService.authenticate(null))
            .thenThrow(new kr.ac.knue.commonfoundation.api.ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));

        mockMvc.perform(get("/api/admin/roles"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void listRolesForNonR09ReturnsForbiddenEnvelope() throws Exception {
        when(authService.authenticate("R01-SESSION"))
            .thenReturn(new SessionPrincipal("R01-SESSION", new AuthenticatedUser("teacher01", List.of("R01"), "SELF")));

        mockMvc.perform(get("/api/admin/roles")
                .cookie(new Cookie("KNUE_SESSION_ID", "R01-SESSION")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    void saveRoleUpdatesLocalManagementFieldsAndReturnsSuccessMessage() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(roleManagementService.saveRole(any(SaveRoleRequest.class)))
            .thenReturn(new SaveRoleResponse("R08", false, "ALL", "역할 관리 저장이 완료되었습니다."));

        mockMvc.perform(post("/api/admin/roles")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"R08\",\"enabled\":false,\"defaultDataScope\":\"ALL\",\"purpose\":\"감사자\",\"grantCriteria\":\"승인 대상\",\"reason\":\"역할 점검\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.roleCode").value("R08"))
            .andExpect(jsonPath("$.data.enabled").value(false))
            .andExpect(jsonPath("$.data.defaultDataScope").value("ALL"))
            .andExpect(jsonPath("$.data.message").value("역할 관리 저장이 완료되었습니다."));

        verify(roleManagementService).saveRole(any(SaveRoleRequest.class));
    }

    @Test
    void saveRoleMissingIdentifierReturnsFieldValidationError() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));

        mockMvc.perform(post("/api/admin/roles")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"enabled\":true,\"defaultDataScope\":\"ALL\",\"reason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.fields.id").exists());
    }
}
