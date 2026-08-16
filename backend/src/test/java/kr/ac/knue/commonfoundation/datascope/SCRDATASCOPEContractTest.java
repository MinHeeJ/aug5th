package kr.ac.knue.commonfoundation.datascope;

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

@WebMvcTest(controllers = DataScopeManagementController.class)
@Import({GlobalApiExceptionHandler.class, CurrentUserContext.class, AuthInterceptor.class, WebMvcConfig.class})
class SCRDATASCOPEContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private DataScopeManagementService dataScopeManagementService;

    @Test
    void openApiFixtureContainsAuthoritativeListDataScopesContract() throws Exception {
        ClassPathResource resource = new ClassPathResource("contracts/openapi.yaml");
        String contract = new String(resource.getInputStream().readAllBytes());

        org.assertj.core.api.Assertions.assertThat(contract)
            .contains("operationId: listDataScopes")
            .contains("/api/admin/data-scopes")
            .contains("x-roles:")
            .contains("- R09")
            .contains("data_scope_permissions");
    }

    @Test
    void listDataScopesReturnsRoleScopeMatrixForR09() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(dataScopeManagementService.listDataScopes(any(DataScopeSearchCondition.class)))
            .thenReturn(new DataScopeListResponse(List.of(new DataScopeListItem(
                10001L,
                "R09",
                "시스템관리자",
                "ALL",
                "전체",
                "KNUE",
                "한국교원대학교",
                "COMMON_FOUNDATION",
                "공통기능 전체",
                "서버 조회조건 전체 범위 강제",
                9
            )), 1, 20, 1, "SCR-DATA-SCOPE", "R09"));

        mockMvc.perform(get("/api/admin/data-scopes")
                .param("page", "1")
                .param("size", "20")
                .param("q", "전체")
                .param("filter", "roleCode=R09;scopeType=ALL;organizationCode=KNUE;businessArea=COMMON_FOUNDATION")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.screenId").value("SCR-DATA-SCOPE"))
            .andExpect(jsonPath("$.data.requiredRole").value("R09"))
            .andExpect(jsonPath("$.data.totalCount").value(1))
            .andExpect(jsonPath("$.data.items[0].dataScopeId").value(10001))
            .andExpect(jsonPath("$.data.items[0].roleCode").value("R09"))
            .andExpect(jsonPath("$.data.items[0].roleName").value("시스템관리자"))
            .andExpect(jsonPath("$.data.items[0].scopeType").value("ALL"))
            .andExpect(jsonPath("$.data.items[0].scopeName").value("전체"))
            .andExpect(jsonPath("$.data.items[0].businessArea").value("COMMON_FOUNDATION"));
    }

    @Test
    void listDataScopesWithoutSessionReturnsUnauthorizedEnvelope() throws Exception {
        when(authService.authenticate(null))
            .thenThrow(new kr.ac.knue.commonfoundation.api.ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));

        mockMvc.perform(get("/api/admin/data-scopes"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void listDataScopesForNonR09ReturnsForbiddenEnvelope() throws Exception {
        when(authService.authenticate("R01-SESSION"))
            .thenReturn(new SessionPrincipal("R01-SESSION", new AuthenticatedUser("teacher01", List.of("R01"), "SELF")));

        mockMvc.perform(get("/api/admin/data-scopes")
                .cookie(new Cookie("KNUE_SESSION_ID", "R01-SESSION")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    void saveDataScopeUpdatesScopeOnlyAndReturnsSuccessMessage() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(dataScopeManagementService.saveDataScope(any(SaveDataScopeRequest.class)))
            .thenReturn(new SaveDataScopeResponse(10001L, "R09", "ALL", "KNUE", "COMMON_FOUNDATION", "데이터 범위 권한 저장이 완료되었습니다."));

        mockMvc.perform(post("/api/admin/data-scopes")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"10001\",\"roleCode\":\"R09\",\"scopeType\":\"ALL\",\"organizationCode\":\"KNUE\",\"businessArea\":\"COMMON_FOUNDATION\",\"reason\":\"데이터 범위 점검\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.dataScopeId").value(10001))
            .andExpect(jsonPath("$.data.roleCode").value("R09"))
            .andExpect(jsonPath("$.data.scopeType").value("ALL"))
            .andExpect(jsonPath("$.data.organizationCode").value("KNUE"))
            .andExpect(jsonPath("$.data.businessArea").value("COMMON_FOUNDATION"))
            .andExpect(jsonPath("$.data.message").value("데이터 범위 권한 저장이 완료되었습니다."));

        verify(dataScopeManagementService).saveDataScope(any(SaveDataScopeRequest.class));
    }

    @Test
    void saveDataScopeMissingIdentifierReturnsFieldValidationError() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));

        mockMvc.perform(post("/api/admin/data-scopes")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roleCode\":\"R09\",\"scopeType\":\"ALL\",\"reason\":\"필수값 검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.fields.id").exists());
    }
}
