package kr.ac.knue.commonfoundation.baseyear;

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

@WebMvcTest(controllers = BaseYearManagementController.class)
@Import({GlobalApiExceptionHandler.class, CurrentUserContext.class, AuthInterceptor.class, WebMvcConfig.class})
class SCRBASEYEARContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private BaseYearManagementService baseYearManagementService;

    @Test
    void openApiFixtureContainsAuthoritativeGetBaseYearsContract() throws Exception {
        ClassPathResource resource = new ClassPathResource("contracts/openapi.yaml");
        String contract = new String(resource.getInputStream().readAllBytes());

        org.assertj.core.api.Assertions.assertThat(contract)
            .contains("operationId: getBaseYears")
            .contains("/api/admin/base-years")
            .contains("x-roles:")
            .contains("- R09")
            .contains("REQ-045");
    }

    @Test
    void getBaseYearsReturnsSourceBackedYearRowsForR09() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(baseYearManagementService.getBaseYears(any(BaseYearSearchCondition.class)))
            .thenReturn(new BaseYearListResponse(List.of(new BaseYearListItem(
                "2026",
                "2026",
                true,
                false,
                true,
                "기준연도는 4자리 연도이며 기본 조회연도는 기준연도 이하로 관리합니다.",
                "기준정보 복사 후 초기화 실행 여부를 서버에서 검증합니다."
            )), 1, 20, 1, "SCR-BASE-YEAR", "R09"));

        mockMvc.perform(get("/api/admin/base-years")
                .param("page", "1")
                .param("size", "20")
                .param("q", "2026")
                .param("filter", "enabled=true")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.screenId").value("SCR-BASE-YEAR"))
            .andExpect(jsonPath("$.data.requiredRole").value("R09"))
            .andExpect(jsonPath("$.data.totalCount").value(1))
            .andExpect(jsonPath("$.data.items[0].baseYear").value("2026"))
            .andExpect(jsonPath("$.data.items[0].defaultQueryYear").value("2026"))
            .andExpect(jsonPath("$.data.items[0].copyBaselineEnabled").value(true))
            .andExpect(jsonPath("$.data.items[0].resetEnabled").value(false))
            .andExpect(jsonPath("$.data.items[0].enabled").value(true));
    }

    @Test
    void getBaseYearsWithoutSessionReturnsUnauthorizedEnvelope() throws Exception {
        when(authService.authenticate(null))
            .thenThrow(new kr.ac.knue.commonfoundation.api.ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));

        mockMvc.perform(get("/api/admin/base-years"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void getBaseYearsForNonR09ReturnsForbiddenEnvelope() throws Exception {
        when(authService.authenticate("R01-SESSION"))
            .thenReturn(new SessionPrincipal("R01-SESSION", new AuthenticatedUser("teacher01", List.of("R01"), "SELF")));

        mockMvc.perform(get("/api/admin/base-years")
                .cookie(new Cookie("KNUE_SESSION_ID", "R01-SESSION")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    void saveBaseYearUpdatesEditablePolicyFieldsAndReturnsSuccessMessage() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(baseYearManagementService.saveBaseYear(any(SaveBaseYearRequest.class)))
            .thenReturn(new SaveBaseYearResponse("2026", "2025", true, false, true, "기준연도 관리 저장이 완료되었습니다."));

        mockMvc.perform(post("/api/admin/base-years")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"2026\",\"defaultQueryYear\":\"2025\",\"copyBaselineEnabled\":true,\"resetEnabled\":false,\"enabled\":true,\"reason\":\"기본 조회연도 조정\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.baseYear").value("2026"))
            .andExpect(jsonPath("$.data.defaultQueryYear").value("2025"))
            .andExpect(jsonPath("$.data.message").value("기준연도 관리 저장이 완료되었습니다."));

        verify(baseYearManagementService).saveBaseYear(any(SaveBaseYearRequest.class));
    }

    @Test
    void saveBaseYearMissingIdentifierReturnsFieldValidationError() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));

        mockMvc.perform(post("/api/admin/base-years")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"defaultQueryYear\":\"2026\",\"copyBaselineEnabled\":true,\"resetEnabled\":false,\"enabled\":true,\"reason\":\"필수값 검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.fields.id").exists());
    }
}
