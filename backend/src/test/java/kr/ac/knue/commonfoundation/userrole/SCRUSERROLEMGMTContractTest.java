package kr.ac.knue.commonfoundation.userrole;

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

@WebMvcTest(controllers = UserRoleManagementController.class)
@Import({GlobalApiExceptionHandler.class, CurrentUserContext.class, AuthInterceptor.class, WebMvcConfig.class})
class SCRUSERROLEMGMTContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserRoleManagementService userRoleManagementService;

    @Test
    void openApiFixtureContainsAuthoritativeListUserRolesContract() throws Exception {
        ClassPathResource resource = new ClassPathResource("contracts/openapi.yaml");
        String contract = new String(resource.getInputStream().readAllBytes());

        org.assertj.core.api.Assertions.assertThat(contract)
            .contains("operationId: listUserRoles")
            .contains("/api/admin/user-roles")
            .contains("x-roles:")
            .contains("- R09");
    }

    @Test
    void listUserRolesReturnsEffectivePeriodAssignmentsForR09() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(userRoleManagementService.listUserRoles(any(UserRoleSearchCondition.class)))
            .thenReturn(new UserRoleListResponse(List.of(new UserRoleListItem(
                1001L,
                "teacher01",
                "김교*",
                "P-2026-001",
                "R01",
                "교원",
                "2026-01-01",
                null,
                "admin",
                "시스템관리자",
                "MANUAL",
                true
            )), 1, 20, 1, "SCR-USER-ROLE-MGMT", "R09"));

        mockMvc.perform(get("/api/admin/user-roles")
                .param("page", "1")
                .param("size", "20")
                .param("q", "김교")
                .param("filter", "active=true;roleCode=R01;assignmentSource=MANUAL")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.screenId").value("SCR-USER-ROLE-MGMT"))
            .andExpect(jsonPath("$.data.requiredRole").value("R09"))
            .andExpect(jsonPath("$.data.totalCount").value(1))
            .andExpect(jsonPath("$.data.items[0].userRoleId").value(1001))
            .andExpect(jsonPath("$.data.items[0].userId").value("teacher01"))
            .andExpect(jsonPath("$.data.items[0].userName").value("김교*"))
            .andExpect(jsonPath("$.data.items[0].roleCode").value("R01"))
            .andExpect(jsonPath("$.data.items[0].roleName").value("교원"))
            .andExpect(jsonPath("$.data.items[0].assignmentSource").value("MANUAL"))
            .andExpect(jsonPath("$.data.items[0].active").value(true));
    }

    @Test
    void listUserRolesWithoutSessionReturnsUnauthorizedEnvelope() throws Exception {
        when(authService.authenticate(null))
            .thenThrow(new kr.ac.knue.commonfoundation.api.ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));

        mockMvc.perform(get("/api/admin/user-roles"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void listUserRolesForNonR09ReturnsForbiddenEnvelope() throws Exception {
        when(authService.authenticate("R01-SESSION"))
            .thenReturn(new SessionPrincipal("R01-SESSION", new AuthenticatedUser("teacher01", List.of("R01"), "SELF")));

        mockMvc.perform(get("/api/admin/user-roles")
                .cookie(new Cookie("KNUE_SESSION_ID", "R01-SESSION")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    void saveUserRoleUpdatesEffectivePeriodAndReturnsSuccessMessage() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(userRoleManagementService.saveUserRole(any(SaveUserRoleRequest.class)))
            .thenReturn(new SaveUserRoleResponse(1001L, false, "2026-12-31", "MANUAL", "사용자 역할 관리 저장이 완료되었습니다."));

        mockMvc.perform(post("/api/admin/user-roles")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"1001\",\"active\":false,\"validTo\":\"2026-12-31\",\"assignmentSource\":\"MANUAL\",\"reason\":\"역할 회수 점검\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.userRoleId").value(1001))
            .andExpect(jsonPath("$.data.active").value(false))
            .andExpect(jsonPath("$.data.validTo").value("2026-12-31"))
            .andExpect(jsonPath("$.data.assignmentSource").value("MANUAL"))
            .andExpect(jsonPath("$.data.message").value("사용자 역할 관리 저장이 완료되었습니다."));

        verify(userRoleManagementService).saveUserRole(any(SaveUserRoleRequest.class));
    }

    @Test
    void saveUserRoleMissingIdentifierReturnsFieldValidationError() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));

        mockMvc.perform(post("/api/admin/user-roles")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"active\":true,\"assignmentSource\":\"MANUAL\",\"reason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.fields.id").exists());
    }
}
