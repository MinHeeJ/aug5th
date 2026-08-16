package kr.ac.knue.commonfoundation.menupermission;

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

@WebMvcTest(controllers = MenuPermissionManagementController.class)
@Import({GlobalApiExceptionHandler.class, CurrentUserContext.class, AuthInterceptor.class, WebMvcConfig.class})
class SCRMENUPERMISSIONContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private MenuPermissionManagementService menuPermissionManagementService;

    @Test
    void openApiFixtureContainsAuthoritativeListMenuPermissionsContract() throws Exception {
        ClassPathResource resource = new ClassPathResource("contracts/openapi.yaml");
        String contract = new String(resource.getInputStream().readAllBytes());

        org.assertj.core.api.Assertions.assertThat(contract)
            .contains("operationId: listMenuPermissions")
            .contains("/api/admin/menu-permissions")
            .contains("x-roles:")
            .contains("- R09");
    }

    @Test
    void listMenuPermissionsReturnsPermissionMatrixForR09() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(menuPermissionManagementService.listMenuPermissions(any(MenuPermissionSearchCondition.class)))
            .thenReturn(new MenuPermissionListResponse(List.of(new MenuPermissionListItem(
                7001L,
                "ROLE",
                "R09",
                "시스템관리자",
                "M-MENU-PERMISSION",
                "메뉴 권한 관리",
                "보안·감사 관리",
                "SCR-MENU-PERMISSION",
                "/admin/security/menu-permissions",
                true,
                "역할 권한",
                15
            )), 1, 20, 1, "SCR-MENU-PERMISSION", "R09"));

        mockMvc.perform(get("/api/admin/menu-permissions")
                .param("page", "1")
                .param("size", "20")
                .param("q", "메뉴 권한")
                .param("filter", "targetType=ROLE;targetId=R09;allowed=true")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.screenId").value("SCR-MENU-PERMISSION"))
            .andExpect(jsonPath("$.data.requiredRole").value("R09"))
            .andExpect(jsonPath("$.data.totalCount").value(1))
            .andExpect(jsonPath("$.data.items[0].menuPermissionId").value(7001))
            .andExpect(jsonPath("$.data.items[0].targetType").value("ROLE"))
            .andExpect(jsonPath("$.data.items[0].targetId").value("R09"))
            .andExpect(jsonPath("$.data.items[0].targetName").value("시스템관리자"))
            .andExpect(jsonPath("$.data.items[0].menuName").value("메뉴 권한 관리"))
            .andExpect(jsonPath("$.data.items[0].screenId").value("SCR-MENU-PERMISSION"))
            .andExpect(jsonPath("$.data.items[0].allowed").value(true));
    }

    @Test
    void listMenuPermissionsWithoutSessionReturnsUnauthorizedEnvelope() throws Exception {
        when(authService.authenticate(null))
            .thenThrow(new kr.ac.knue.commonfoundation.api.ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));

        mockMvc.perform(get("/api/admin/menu-permissions"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void listMenuPermissionsForNonR09ReturnsForbiddenEnvelope() throws Exception {
        when(authService.authenticate("R01-SESSION"))
            .thenReturn(new SessionPrincipal("R01-SESSION", new AuthenticatedUser("teacher01", List.of("R01"), "SELF")));

        mockMvc.perform(get("/api/admin/menu-permissions")
                .cookie(new Cookie("KNUE_SESSION_ID", "R01-SESSION")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    void saveMenuPermissionUpdatesAllowedAndReturnsSuccessMessage() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(menuPermissionManagementService.saveMenuPermission(any(SaveMenuPermissionRequest.class)))
            .thenReturn(new SaveMenuPermissionResponse(7001L, false, "ROLE", "R09", "메뉴 권한 관리 저장이 완료되었습니다."));

        mockMvc.perform(post("/api/admin/menu-permissions")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"7001\",\"allowed\":false,\"targetType\":\"ROLE\",\"targetId\":\"R09\",\"reason\":\"메뉴 노출 점검\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.menuPermissionId").value(7001))
            .andExpect(jsonPath("$.data.allowed").value(false))
            .andExpect(jsonPath("$.data.targetType").value("ROLE"))
            .andExpect(jsonPath("$.data.targetId").value("R09"))
            .andExpect(jsonPath("$.data.message").value("메뉴 권한 관리 저장이 완료되었습니다."));

        verify(menuPermissionManagementService).saveMenuPermission(any(SaveMenuPermissionRequest.class));
    }

    @Test
    void saveMenuPermissionMissingIdentifierReturnsFieldValidationError() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));

        mockMvc.perform(post("/api/admin/menu-permissions")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"allowed\":true,\"targetType\":\"ROLE\",\"targetId\":\"R09\",\"reason\":\"필수값 검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.fields.id").exists());
    }
}
