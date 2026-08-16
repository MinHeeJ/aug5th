package kr.ac.knue.commonfoundation.systemconfig;

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

@WebMvcTest(controllers = SystemConfigurationManagementController.class)
@Import({GlobalApiExceptionHandler.class, CurrentUserContext.class, AuthInterceptor.class, WebMvcConfig.class})
class SCRSYSTEMCONFIGContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private SystemConfigurationManagementService systemConfigurationManagementService;

    @Test
    void openApiFixtureContainsAuthoritativeGetSystemConfigurationsContract() throws Exception {
        ClassPathResource resource = new ClassPathResource("contracts/openapi.yaml");
        String contract = new String(resource.getInputStream().readAllBytes());

        org.assertj.core.api.Assertions.assertThat(contract)
            .contains("operationId: getSystemConfigurations")
            .contains("/api/admin/system-configurations")
            .contains("x-roles:")
            .contains("- R09")
            .contains("REQ-042");
    }

    @Test
    void getSystemConfigurationsReturnsSourceBackedConfigurationRowsForR09() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(systemConfigurationManagementService.getSystemConfigurations(any(SystemConfigurationSearchCondition.class)))
            .thenReturn(new SystemConfigurationListResponse(List.of(new SystemConfigurationListItem(
                "SESSION_IDLE_MINUTES",
                "30",
                "분",
                "5-240",
                true,
                "전체 사용자 공통 적용",
                "세션 유휴시간은 사용자·업무별 개별값 없이 전역으로 적용됩니다."
            )), 1, 20, 1, "SCR-SYSTEM-CONFIG", "R09"));

        mockMvc.perform(get("/api/admin/system-configurations")
                .param("page", "1")
                .param("size", "20")
                .param("q", "세션")
                .param("filter", "enabled=true")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.screenId").value("SCR-SYSTEM-CONFIG"))
            .andExpect(jsonPath("$.data.requiredRole").value("R09"))
            .andExpect(jsonPath("$.data.totalCount").value(1))
            .andExpect(jsonPath("$.data.items[0].configKey").value("SESSION_IDLE_MINUTES"))
            .andExpect(jsonPath("$.data.items[0].configValue").value("30"))
            .andExpect(jsonPath("$.data.items[0].unit").value("분"))
            .andExpect(jsonPath("$.data.items[0].valueRange").value("5-240"))
            .andExpect(jsonPath("$.data.items[0].enabled").value(true));
    }

    @Test
    void getSystemConfigurationsWithoutSessionReturnsUnauthorizedEnvelope() throws Exception {
        when(authService.authenticate(null))
            .thenThrow(new kr.ac.knue.commonfoundation.api.ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));

        mockMvc.perform(get("/api/admin/system-configurations"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void getSystemConfigurationsForNonR09ReturnsForbiddenEnvelope() throws Exception {
        when(authService.authenticate("R01-SESSION"))
            .thenReturn(new SessionPrincipal("R01-SESSION", new AuthenticatedUser("teacher01", List.of("R01"), "SELF")));

        mockMvc.perform(get("/api/admin/system-configurations")
                .cookie(new Cookie("KNUE_SESSION_ID", "R01-SESSION")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    void saveSystemConfigurationUpdatesEditableFieldsAndReturnsSuccessMessage() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(systemConfigurationManagementService.saveSystemConfiguration(any(SaveSystemConfigurationRequest.class)))
            .thenReturn(new SaveSystemConfigurationResponse("SESSION_IDLE_MINUTES", "45", "분", "5-240", true, "공통 환경설정 저장이 완료되었습니다."));

        mockMvc.perform(post("/api/admin/system-configurations")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"SESSION_IDLE_MINUTES\",\"configValue\":\"45\",\"enabled\":true,\"reason\":\"세션 정책 조정\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.configKey").value("SESSION_IDLE_MINUTES"))
            .andExpect(jsonPath("$.data.configValue").value("45"))
            .andExpect(jsonPath("$.data.unit").value("분"))
            .andExpect(jsonPath("$.data.message").value("공통 환경설정 저장이 완료되었습니다."));

        verify(systemConfigurationManagementService).saveSystemConfiguration(any(SaveSystemConfigurationRequest.class));
    }

    @Test
    void saveSystemConfigurationMissingIdentifierReturnsFieldValidationError() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));

        mockMvc.perform(post("/api/admin/system-configurations")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"configValue\":\"45\",\"enabled\":true,\"reason\":\"필수값 검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.fields.id").exists());
    }
}
