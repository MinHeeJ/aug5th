package kr.ac.knue.commonfoundation.batchexecution;

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

@WebMvcTest(controllers = BatchExecutionManagementController.class)
@Import({GlobalApiExceptionHandler.class, CurrentUserContext.class, AuthInterceptor.class, WebMvcConfig.class})
class SCRBATCHEXECUTIONContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private BatchExecutionManagementService batchExecutionManagementService;

    @Test
    void openApiFixtureContainsAuthoritativeBatchExecutionContracts() throws Exception {
        ClassPathResource resource = new ClassPathResource("contracts/openapi.yaml");
        String contract = new String(resource.getInputStream().readAllBytes());

        org.assertj.core.api.Assertions.assertThat(contract)
            .contains("/api/admin/batch-executions")
            .contains("operationId: runBatch")
            .contains("/api/admin/batch-executions/{executionId}/stop")
            .contains("operationId: stopBatch")
            .contains("/api/admin/batch-executions/{executionId}/rerun")
            .contains("operationId: rerunBatch")
            .contains("REQ-096")
            .contains("REQ-097")
            .contains("REQ-098")
            .contains("REQ-099")
            .contains("batch_executions");
    }

    @Test
    void listBatchExecutionsReturnsExecutionHistoryForR09() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(batchExecutionManagementService.listBatchExecutions(any(BatchExecutionSearchCondition.class)))
            .thenReturn(new BatchExecutionListResponse(List.of(new BatchExecutionListItem(
                10L,
                "COMMON-AUDIT-ROLLUP",
                "감사 로그 일별 집계",
                "{\"businessArea\":\"COMMON_FOUNDATION\"}",
                "월말 수동 점검",
                "RUNNING",
                "실행중",
                "admin",
                "관리자",
                "중지 가능: 실행 중 배치이며 R09 사유 입력 필요"
            )), 1, 20, 1, "SCR-BATCH-EXECUTION", "R09"));

        mockMvc.perform(get("/api/admin/batch-executions")
                .param("page", "1")
                .param("size", "20")
                .param("q", "AUDIT")
                .param("filter", "batchId=COMMON-AUDIT-ROLLUP;status=RUNNING")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.screenId").value("SCR-BATCH-EXECUTION"))
            .andExpect(jsonPath("$.data.requiredRole").value("R09"))
            .andExpect(jsonPath("$.data.totalCount").value(1))
            .andExpect(jsonPath("$.data.items[0].batchExecutionId").value(10))
            .andExpect(jsonPath("$.data.items[0].batchId").value("COMMON-AUDIT-ROLLUP"))
            .andExpect(jsonPath("$.data.items[0].executionStatusName").value("실행중"));
    }

    @Test
    void runBatchRecordsExecutionReasonAndReturnsExecutionId() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(batchExecutionManagementService.runBatch(any(RunBatchRequest.class)))
            .thenReturn(new BatchExecutionActionResponse(11L, "COMMON-AUDIT-ROLLUP", "RUNNING", "배치 수동실행 요청이 기록되었습니다."));

        mockMvc.perform(post("/api/admin/batch-executions")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"COMMON-AUDIT-ROLLUP\",\"parameters\":\"{\\\"businessArea\\\":\\\"COMMON_FOUNDATION\\\"}\",\"reason\":\"월말 수동 점검\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.batchExecutionId").value(11))
            .andExpect(jsonPath("$.data.executionStatus").value("RUNNING"));

        verify(batchExecutionManagementService).runBatch(any(RunBatchRequest.class));
    }

    @Test
    void stopBatchRequiresReasonAndTransitionsRunningExecution() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(batchExecutionManagementService.stopBatch(eq(10L), any(BatchExecutionActionRequest.class)))
            .thenReturn(new BatchExecutionActionResponse(10L, "COMMON-AUDIT-ROLLUP", "CANCELLED", "배치 중지 요청이 기록되었습니다."));

        mockMvc.perform(post("/api/admin/batch-executions/10/stop")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"장시간 실행 중지\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.batchExecutionId").value(10))
            .andExpect(jsonPath("$.data.executionStatus").value("CANCELLED"));
    }

    @Test
    void rerunBatchRequiresReasonAndCreatesRetryExecutionSignal() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(batchExecutionManagementService.rerunBatch(eq(10L), any(BatchExecutionActionRequest.class)))
            .thenReturn(new BatchExecutionActionResponse(12L, "COMMON-AUDIT-ROLLUP", "PENDING", "배치 재실행 요청이 기록되었습니다."));

        mockMvc.perform(post("/api/admin/batch-executions/10/rerun")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"실패 원인 조치 후 재실행\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.batchExecutionId").value(12))
            .andExpect(jsonPath("$.data.batchId").value("COMMON-AUDIT-ROLLUP"))
            .andExpect(jsonPath("$.data.executionStatus").value("PENDING"));

        verify(batchExecutionManagementService).rerunBatch(eq(10L), any(BatchExecutionActionRequest.class));
    }

    @Test
    void runBatchMissingIdentifierReturnsFieldValidationError() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));

        mockMvc.perform(post("/api/admin/batch-executions")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"parameters\":\"{}\",\"reason\":\"식별자 누락 검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.fields.id").exists());
    }

    @Test
    void listBatchExecutionsForNonR09ReturnsForbiddenEnvelope() throws Exception {
        when(authService.authenticate("R01-SESSION"))
            .thenReturn(new SessionPrincipal("R01-SESSION", new AuthenticatedUser("teacher01", List.of("R01"), "SELF")));

        mockMvc.perform(get("/api/admin/batch-executions")
                .cookie(new Cookie("KNUE_SESSION_ID", "R01-SESSION")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }
}
