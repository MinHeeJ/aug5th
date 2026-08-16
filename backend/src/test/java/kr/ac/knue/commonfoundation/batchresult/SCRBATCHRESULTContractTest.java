package kr.ac.knue.commonfoundation.batchresult;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
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
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = BatchResultManagementController.class)
@Import({GlobalApiExceptionHandler.class, CurrentUserContext.class, AuthInterceptor.class, WebMvcConfig.class})
class SCRBATCHRESULTContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private BatchResultManagementService batchResultManagementService;

    @Test
    void openApiFixtureContainsAuthoritativeBatchResultIntent() throws Exception {
        ClassPathResource resource = new ClassPathResource("contracts/openapi.yaml");
        String contract = new String(resource.getInputStream().readAllBytes());

        org.assertj.core.api.Assertions.assertThat(contract)
            .contains("REQ-100")
            .contains("REQ-101")
            .contains("REQ-102")
            .contains("REQ-103")
            .contains("배치 결과 조회")
            .contains("배치 결과 조회·로그 조회");
    }

    @Test
    void listBatchResultsReturnsReadOnlyCountsAndLinkedLogForR09() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(batchResultManagementService.listBatchResults(any(BatchResultSearchCondition.class)))
            .thenReturn(new BatchResultListResponse(List.of(new BatchResultListItem(
                31L,
                10L,
                "COMMON-AUDIT-ROLLUP",
                "감사 로그 일별 집계",
                LocalDateTime.of(2026, 8, 16, 9, 0),
                LocalDateTime.of(2026, 8, 16, 9, 2, 5),
                127,
                120,
                2,
                5,
                125000L,
                501L,
                "batch-COMMON-AUDIT-ROLLUP-10.log",
                "FAILED",
                "실패",
                "로그파일은 배치 실행ID 10에 연결된 파일만 조회합니다.",
                "결과 조회 화면에서는 재실행하거나 실패자료·로그파일을 수정·삭제하지 않습니다."
            )), 1, 20, 1, "SCR-BATCH-RESULT", "R09"));

        mockMvc.perform(get("/api/admin/batch-results")
                .param("page", "1")
                .param("size", "20")
                .param("q", "AUDIT")
                .param("filter", "batchId=COMMON-AUDIT-ROLLUP;resultStatus=FAILED")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.screenId").value("SCR-BATCH-RESULT"))
            .andExpect(jsonPath("$.data.requiredRole").value("R09"))
            .andExpect(jsonPath("$.data.totalCount").value(1))
            .andExpect(jsonPath("$.data.items[0].batchResultId").value(31))
            .andExpect(jsonPath("$.data.items[0].batchExecutionId").value(10))
            .andExpect(jsonPath("$.data.items[0].totalCount").value(127))
            .andExpect(jsonPath("$.data.items[0].successCount").value(120))
            .andExpect(jsonPath("$.data.items[0].failureCount").value(2))
            .andExpect(jsonPath("$.data.items[0].excludedCount").value(5))
            .andExpect(jsonPath("$.data.items[0].durationMs").value(125000))
            .andExpect(jsonPath("$.data.items[0].logFileName").value("batch-COMMON-AUDIT-ROLLUP-10.log"))
            .andExpect(jsonPath("$.data.items[0].operationRule").value("결과 조회 화면에서는 재실행하거나 실패자료·로그파일을 수정·삭제하지 않습니다."));
    }

    @Test
    void getBatchResultLogReturnsOnlyLogLinkedToExecution() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(batchResultManagementService.getBatchResultLog(eq(31L)))
            .thenReturn(new BatchResultLogResponse(
                31L,
                10L,
                501L,
                "batch-COMMON-AUDIT-ROLLUP-10.log",
                "배치 실행ID 10 로그 파일 조회가 허용되었습니다.",
                "결과 조회 화면에서는 로그를 수정·삭제하지 않습니다."
            ));

        mockMvc.perform(get("/api/admin/batch-results/31/log")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.batchResultId").value(31))
            .andExpect(jsonPath("$.data.batchExecutionId").value(10))
            .andExpect(jsonPath("$.data.logFileId").value(501))
            .andExpect(jsonPath("$.data.logFileName").value("batch-COMMON-AUDIT-ROLLUP-10.log"));

        verify(batchResultManagementService).getBatchResultLog(31L);
    }

    @Test
    void getBatchResultLogForMissingResultReturnsNotFoundEnvelope() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(batchResultManagementService.getBatchResultLog(eq(999L)))
            .thenThrow(new kr.ac.knue.commonfoundation.api.ApiException(org.springframework.http.HttpStatus.NOT_FOUND, "NOT_FOUND", "배치 결과를 찾을 수 없습니다."));

        mockMvc.perform(get("/api/admin/batch-results/999/log")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION")))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    void listBatchResultsForNonR09ReturnsForbiddenEnvelope() throws Exception {
        when(authService.authenticate("R01-SESSION"))
            .thenReturn(new SessionPrincipal("R01-SESSION", new AuthenticatedUser("teacher01", List.of("R01"), "SELF")));

        mockMvc.perform(get("/api/admin/batch-results")
                .cookie(new Cookie("KNUE_SESSION_ID", "R01-SESSION")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }
}
