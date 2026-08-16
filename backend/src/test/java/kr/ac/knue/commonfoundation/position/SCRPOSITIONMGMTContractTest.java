package kr.ac.knue.commonfoundation.position;

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

@WebMvcTest(controllers = PositionManagementController.class)
@Import({GlobalApiExceptionHandler.class, CurrentUserContext.class, AuthInterceptor.class, WebMvcConfig.class})
class SCRPOSITIONMGMTContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private PositionManagementService positionManagementService;

    @Test
    void openApiFixtureContainsAuthoritativeListPositionsContract() throws Exception {
        ClassPathResource resource = new ClassPathResource("contracts/openapi.yaml");
        String contract = new String(resource.getInputStream().readAllBytes());

        org.assertj.core.api.Assertions.assertThat(contract)
            .contains("operationId: listPositions")
            .contains("/api/admin/positions")
            .contains("x-roles:")
            .contains("- R09");
    }

    @Test
    void listPositionsReturnsAssignmentAndPagingEnvelopeForR09() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(positionManagementService.listPositions(any(PositionSearchCondition.class)))
            .thenReturn(new PositionListResponse(List.of(new PositionListItem(
                1L,
                "DEPT_HEAD",
                "학과장",
                "teacher01",
                "김교*",
                "P-2026-001",
                "KNUE-EDU",
                "교육학과",
                "2026-01-01",
                null,
                true
            )), 1, 20, 1, "SCR-POSITION-MGMT", "R09"));

        mockMvc.perform(get("/api/admin/positions")
                .param("page", "1")
                .param("size", "20")
                .param("q", "학과장")
                .param("filter", "organizationCode=KNUE-EDU;active=true")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.screenId").value("SCR-POSITION-MGMT"))
            .andExpect(jsonPath("$.data.requiredRole").value("R09"))
            .andExpect(jsonPath("$.data.totalCount").value(1))
            .andExpect(jsonPath("$.data.items[0].positionId").value(1))
            .andExpect(jsonPath("$.data.items[0].positionCode").value("DEPT_HEAD"))
            .andExpect(jsonPath("$.data.items[0].positionName").value("학과장"))
            .andExpect(jsonPath("$.data.items[0].userId").value("teacher01"))
            .andExpect(jsonPath("$.data.items[0].organizationCode").value("KNUE-EDU"))
            .andExpect(jsonPath("$.data.items[0].organizationName").value("교육학과"))
            .andExpect(jsonPath("$.data.items[0].validFrom").value("2026-01-01"))
            .andExpect(jsonPath("$.data.items[0].active").value(true));
    }

    @Test
    void listPositionsWithoutSessionReturnsUnauthorizedEnvelope() throws Exception {
        when(authService.authenticate(null))
            .thenThrow(new kr.ac.knue.commonfoundation.api.ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));

        mockMvc.perform(get("/api/admin/positions"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void listPositionsForNonR09ReturnsForbiddenEnvelope() throws Exception {
        when(authService.authenticate("R01-SESSION"))
            .thenReturn(new SessionPrincipal("R01-SESSION", new AuthenticatedUser("teacher01", List.of("R01"), "SELF")));

        mockMvc.perform(get("/api/admin/positions")
                .cookie(new Cookie("KNUE_SESSION_ID", "R01-SESSION")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    void savePositionUpdatesAssignmentPeriodAndReturnsSuccessMessage() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(positionManagementService.savePosition(any(SavePositionRequest.class)))
            .thenReturn(new SavePositionResponse(1L, false, "2026-12-31", "보직 관리 저장이 완료되었습니다."));

        mockMvc.perform(post("/api/admin/positions")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"1\",\"active\":false,\"validTo\":\"2026-12-31\",\"reason\":\"보직 종료 반영\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.positionId").value(1))
            .andExpect(jsonPath("$.data.active").value(false))
            .andExpect(jsonPath("$.data.validTo").value("2026-12-31"))
            .andExpect(jsonPath("$.data.message").value("보직 관리 저장이 완료되었습니다."));

        verify(positionManagementService).savePosition(any(SavePositionRequest.class));
    }

    @Test
    void savePositionMissingIdentifierReturnsFieldValidationError() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));

        mockMvc.perform(post("/api/admin/positions")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"active\":true,\"reason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.fields.id").exists());
    }
}
