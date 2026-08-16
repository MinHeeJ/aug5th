package kr.ac.knue.commonfoundation.auditlog;

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

@WebMvcTest(controllers = AuditLogManagementController.class)
@Import({GlobalApiExceptionHandler.class, CurrentUserContext.class, AuthInterceptor.class, WebMvcConfig.class})
class SCRAUDITLOGContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private AuditLogManagementService auditLogManagementService;

    @Test
    void openApiFixtureContainsAuthoritativeAuditLogContracts() throws Exception {
        ClassPathResource resource = new ClassPathResource("contracts/openapi.yaml");
        String contract = new String(resource.getInputStream().readAllBytes());

        org.assertj.core.api.Assertions.assertThat(contract)
            .contains("/api/admin/audit-logs")
            .contains("operationId: listAuditLogs")
            .contains("operationId: saveAuditLogs")
            .contains("x-roles:")
            .contains("- R09")
            .contains("REQ-087")
            .contains("REQ-088")
            .contains("REQ-089")
            .contains("REQ-090")
            .contains("REQ-091")
            .contains("audit_logs");
    }

    @Test
    void listAuditLogsReturnsImmutableAuditEntriesForR09() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(auditLogManagementService.listAuditLogs(any(AuditLogSearchCondition.class)))
            .thenReturn(new AuditLogListResponse(List.of(new AuditLogListItem(
                1001L,
                "AUTHORIZATION",
                "권한",
                "roles:R09",
                "admin",
                "{\"roleCode\":\"R09\"}",
                "{\"allowed\":true}",
                "SUCCESS",
                "성공",
                "감사로그 원문은 수정·삭제할 수 없으며 상세에서 변경 전후값을 조회합니다."
            )), 1, 20, 1, "SCR-AUDIT-LOG", "R09"));

        mockMvc.perform(get("/api/admin/audit-logs")
                .param("page", "1")
                .param("size", "20")
                .param("q", "roles")
                .param("filter", "logType=AUTHORIZATION;result=SUCCESS;actorId=admin")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.screenId").value("SCR-AUDIT-LOG"))
            .andExpect(jsonPath("$.data.requiredRole").value("R09"))
            .andExpect(jsonPath("$.data.totalCount").value(1))
            .andExpect(jsonPath("$.data.items[0].auditLogId").value(1001))
            .andExpect(jsonPath("$.data.items[0].logType").value("AUTHORIZATION"))
            .andExpect(jsonPath("$.data.items[0].targetKey").value("roles:R09"))
            .andExpect(jsonPath("$.data.items[0].actorId").value("admin"))
            .andExpect(jsonPath("$.data.items[0].resultName").value("성공"));
    }

    @Test
    void saveAuditLogAppendsManagementAuditRecordWithoutMutatingSelectedEntry() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(auditLogManagementService.saveAuditLog(any(SaveAuditLogRequest.class)))
            .thenReturn(new SaveAuditLogResponse(2002L, "audit_logs:1001", "SUCCESS", "감사 로그 관리 확인 이력이 기록되었습니다."));

        mockMvc.perform(post("/api/admin/audit-logs")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"1001\",\"reason\":\"중요정보 조회 로그 확인\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.auditLogId").value(2002))
            .andExpect(jsonPath("$.data.targetKey").value("audit_logs:1001"))
            .andExpect(jsonPath("$.data.result").value("SUCCESS"));

        verify(auditLogManagementService).saveAuditLog(any(SaveAuditLogRequest.class));
    }

    @Test
    void saveAuditLogMissingIdentifierReturnsFieldValidationError() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));

        mockMvc.perform(post("/api/admin/audit-logs")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"확인 사유\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.fields.id").exists());
    }

    @Test
    void listAuditLogsForNonR09ReturnsForbiddenEnvelope() throws Exception {
        when(authService.authenticate("R01-SESSION"))
            .thenReturn(new SessionPrincipal("R01-SESSION", new AuthenticatedUser("teacher01", List.of("R01"), "SELF")));

        mockMvc.perform(get("/api/admin/audit-logs")
                .cookie(new Cookie("KNUE_SESSION_ID", "R01-SESSION")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }
}
