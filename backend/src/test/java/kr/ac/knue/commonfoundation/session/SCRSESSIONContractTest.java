package kr.ac.knue.commonfoundation.session;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

@WebMvcTest(controllers = SessionManagementController.class)
@Import({GlobalApiExceptionHandler.class, CurrentUserContext.class, AuthInterceptor.class, WebMvcConfig.class})
class SCRSESSIONContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private SessionManagementService sessionManagementService;

    @Test
    void openApiFixtureContainsAuthoritativeSessionContracts() throws Exception {
        ClassPathResource resource = new ClassPathResource("contracts/openapi.yaml");
        String contract = new String(resource.getInputStream().readAllBytes());

        org.assertj.core.api.Assertions.assertThat(contract)
            .contains("/api/admin/sessions")
            .contains("operationId: listActiveSessions")
            .contains("operationId: saveActiveSessions")
            .contains("/api/admin/sessions/{sessionId}/terminate")
            .contains("operationId: terminateSession")
            .contains("x-roles:")
            .contains("- R09")
            .contains("REQ-082")
            .contains("REQ-083")
            .contains("REQ-084")
            .contains("REQ-085")
            .contains("REQ-086")
            .contains("user_sessions");
    }

    @Test
    void listActiveSessionsReturnsCurrentSessionStatusForR09() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(sessionManagementService.listActiveSessions(any(SessionSearchCondition.class)))
            .thenReturn(new SessionListResponse(List.of(new SessionListItem(
                "SESSION-001",
                "admin",
                "관리자",
                "2026-08-16T09:00:00",
                "2026-08-16T09:30:00",
                "127.0.0.1",
                "ACTIVE",
                "활성",
                null,
                null,
                "강제종료 가능: 활성 세션이며 R09 사유 입력 필요"
            )), 1, 20, 1, "SCR-SESSION", "R09"));

        mockMvc.perform(get("/api/admin/sessions")
                .param("page", "1")
                .param("size", "20")
                .param("q", "admin")
                .param("filter", "status=ACTIVE;ip=127.0.0.1")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.screenId").value("SCR-SESSION"))
            .andExpect(jsonPath("$.data.requiredRole").value("R09"))
            .andExpect(jsonPath("$.data.totalCount").value(1))
            .andExpect(jsonPath("$.data.items[0].sessionId").value("SESSION-001"))
            .andExpect(jsonPath("$.data.items[0].userId").value("admin"))
            .andExpect(jsonPath("$.data.items[0].sessionStatus").value("ACTIVE"))
            .andExpect(jsonPath("$.data.items[0].sessionStatusName").value("활성"));
    }

    @Test
    void saveActiveSessionTerminatesSelectedSessionAndWritesImmutableHistory() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(sessionManagementService.saveActiveSession(any(SaveSessionRequest.class)))
            .thenReturn(new SaveSessionResponse("SESSION-001", "TERMINATED", "FORCED", "접속현황 관리 세션 강제종료가 완료되었습니다."));

        mockMvc.perform(post("/api/admin/sessions")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"SESSION-001\",\"reason\":\"보안 점검으로 강제 종료\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.sessionId").value("SESSION-001"))
            .andExpect(jsonPath("$.data.sessionStatus").value("TERMINATED"))
            .andExpect(jsonPath("$.data.terminationType").value("FORCED"));

        verify(sessionManagementService).saveActiveSession(any(SaveSessionRequest.class));
    }

    @Test
    void terminateSessionPathUsesSelectedSessionIdAndRequiresReason() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(sessionManagementService.terminateSession(eq("SESSION-001"), any(TerminateSessionRequest.class)))
            .thenReturn(new SaveSessionResponse("SESSION-001", "TERMINATED", "FORCED", "세션 강제종료가 완료되었습니다."));

        mockMvc.perform(post("/api/admin/sessions/SESSION-001/terminate")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"의심 접속 종료\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.sessionId").value("SESSION-001"))
            .andExpect(jsonPath("$.data.terminationType").value("FORCED"));
    }

    @Test
    void saveActiveSessionMissingReasonReturnsFieldValidationError() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));

        mockMvc.perform(post("/api/admin/sessions")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"SESSION-001\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.fields.reason").exists());
    }

    @Test
    void listActiveSessionsForNonR09ReturnsForbiddenEnvelope() throws Exception {
        when(authService.authenticate("R01-SESSION"))
            .thenReturn(new SessionPrincipal("R01-SESSION", new AuthenticatedUser("teacher01", List.of("R01"), "SELF")));

        mockMvc.perform(get("/api/admin/sessions")
                .cookie(new Cookie("KNUE_SESSION_ID", "R01-SESSION")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }
}
