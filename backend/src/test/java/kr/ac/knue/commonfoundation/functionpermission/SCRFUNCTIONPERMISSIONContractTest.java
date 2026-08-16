package kr.ac.knue.commonfoundation.functionpermission;

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

@WebMvcTest(controllers = FunctionPermissionManagementController.class)
@Import({GlobalApiExceptionHandler.class, CurrentUserContext.class, AuthInterceptor.class, WebMvcConfig.class})
class SCRFUNCTIONPERMISSIONContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private FunctionPermissionManagementService functionPermissionManagementService;

    @Test
    void openApiFixtureContainsAuthoritativeListFunctionPermissionsContract() throws Exception {
        ClassPathResource resource = new ClassPathResource("contracts/openapi.yaml");
        String contract = new String(resource.getInputStream().readAllBytes());

        org.assertj.core.api.Assertions.assertThat(contract)
            .contains("operationId: listFunctionPermissions")
            .contains("/api/admin/function-permissions")
            .contains("x-roles:")
            .contains("- R09")
            .contains("function_permissions");
    }

    @Test
    void listFunctionPermissionsReturnsRoleScreenActionMatrixForR09() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(functionPermissionManagementService.listFunctionPermissions(any(FunctionPermissionSearchCondition.class)))
            .thenReturn(new FunctionPermissionListResponse(List.of(new FunctionPermissionListItem(
                9001L,
                "R09",
                "시스템관리자",
                "SCR-FUNCTION-PERMISSION",
                "기능 권한 관리",
                "M-FUNCTION-PERMISSION",
                "기능 권한 관리",
                "UPDATE",
                "수정",
                true,
                "시스템관리자 전체 기능",
                16
            )), 1, 20, 1, "SCR-FUNCTION-PERMISSION", "R09"));

        mockMvc.perform(get("/api/admin/function-permissions")
                .param("page", "1")
                .param("size", "20")
                .param("q", "기능 권한")
                .param("filter", "roleCode=R09;screenId=SCR-FUNCTION-PERMISSION;actionCode=UPDATE;allowed=true")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.screenId").value("SCR-FUNCTION-PERMISSION"))
            .andExpect(jsonPath("$.data.requiredRole").value("R09"))
            .andExpect(jsonPath("$.data.totalCount").value(1))
            .andExpect(jsonPath("$.data.items[0].functionPermissionId").value(9001))
            .andExpect(jsonPath("$.data.items[0].roleCode").value("R09"))
            .andExpect(jsonPath("$.data.items[0].roleName").value("시스템관리자"))
            .andExpect(jsonPath("$.data.items[0].screenId").value("SCR-FUNCTION-PERMISSION"))
            .andExpect(jsonPath("$.data.items[0].actionCode").value("UPDATE"))
            .andExpect(jsonPath("$.data.items[0].actionName").value("수정"))
            .andExpect(jsonPath("$.data.items[0].allowed").value(true));
    }

    @Test
    void listFunctionPermissionsWithoutSessionReturnsUnauthorizedEnvelope() throws Exception {
        when(authService.authenticate(null))
            .thenThrow(new kr.ac.knue.commonfoundation.api.ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));

        mockMvc.perform(get("/api/admin/function-permissions"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void listFunctionPermissionsForNonR09ReturnsForbiddenEnvelope() throws Exception {
        when(authService.authenticate("R01-SESSION"))
            .thenReturn(new SessionPrincipal("R01-SESSION", new AuthenticatedUser("teacher01", List.of("R01"), "SELF")));

        mockMvc.perform(get("/api/admin/function-permissions")
                .cookie(new Cookie("KNUE_SESSION_ID", "R01-SESSION")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    void saveFunctionPermissionUpdatesSingleActionOnlyAndReturnsSuccessMessage() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(functionPermissionManagementService.saveFunctionPermission(any(SaveFunctionPermissionRequest.class)))
            .thenReturn(new SaveFunctionPermissionResponse(9001L, false, "R09", "SCR-FUNCTION-PERMISSION", "UPDATE", "기능 권한 관리 저장이 완료되었습니다."));

        mockMvc.perform(post("/api/admin/function-permissions")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"9001\",\"allowed\":false,\"roleCode\":\"R09\",\"screenId\":\"SCR-FUNCTION-PERMISSION\",\"actionCode\":\"UPDATE\",\"reason\":\"수정 기능 권한 점검\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.functionPermissionId").value(9001))
            .andExpect(jsonPath("$.data.allowed").value(false))
            .andExpect(jsonPath("$.data.roleCode").value("R09"))
            .andExpect(jsonPath("$.data.screenId").value("SCR-FUNCTION-PERMISSION"))
            .andExpect(jsonPath("$.data.actionCode").value("UPDATE"))
            .andExpect(jsonPath("$.data.message").value("기능 권한 관리 저장이 완료되었습니다."));

        verify(functionPermissionManagementService).saveFunctionPermission(any(SaveFunctionPermissionRequest.class));
    }

    @Test
    void saveFunctionPermissionMissingIdentifierReturnsFieldValidationError() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));

        mockMvc.perform(post("/api/admin/function-permissions")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"allowed\":true,\"roleCode\":\"R09\",\"screenId\":\"SCR-FUNCTION-PERMISSION\",\"actionCode\":\"UPDATE\",\"reason\":\"필수값 검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.fields.id").exists());
    }
}
