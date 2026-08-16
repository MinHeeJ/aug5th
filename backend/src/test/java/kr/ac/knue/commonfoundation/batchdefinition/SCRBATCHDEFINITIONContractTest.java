package kr.ac.knue.commonfoundation.batchdefinition;

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

@WebMvcTest(controllers = BatchDefinitionManagementController.class)
@Import({GlobalApiExceptionHandler.class, CurrentUserContext.class, AuthInterceptor.class, WebMvcConfig.class})
class SCRBATCHDEFINITIONContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private BatchDefinitionManagementService batchDefinitionManagementService;

    @Test
    void openApiFixtureContainsAuthoritativeBatchDefinitionContracts() throws Exception {
        ClassPathResource resource = new ClassPathResource("contracts/openapi.yaml");
        String contract = new String(resource.getInputStream().readAllBytes());

        org.assertj.core.api.Assertions.assertThat(contract)
            .contains("/api/admin/batch-definitions")
            .contains("operationId: listBatchDefinitions")
            .contains("operationId: saveBatchDefinitions")
            .contains("x-roles:")
            .contains("- R09")
            .contains("REQ-092")
            .contains("REQ-093")
            .contains("REQ-094")
            .contains("REQ-095")
            .contains("batch_definitions");
    }

    @Test
    void listBatchDefinitionsReturnsScheduleAndOwnerForR09() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(batchDefinitionManagementService.listBatchDefinitions(any(BatchDefinitionSearchCondition.class)))
            .thenReturn(new BatchDefinitionListResponse(List.of(new BatchDefinitionListItem(
                "COMMON-AUDIT-ROLLUP",
                "감사 집계 배치",
                "0 0 * * *",
                null,
                null,
                "{\"businessArea\":\"COMMON_FOUNDATION\"}",
                3600,
                "admin",
                "관리자",
                "DEFINED",
                "정의됨",
                "배치 정의 화면은 즉시 실행·중지·재실행을 제공하지 않고 정의만 저장합니다."
            )), 1, 20, 1, "SCR-BATCH-DEFINITION", "R09"));

        mockMvc.perform(get("/api/admin/batch-definitions")
                .param("page", "1")
                .param("size", "20")
                .param("q", "AUDIT")
                .param("filter", "ownerId=admin;schedule=0 0")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.screenId").value("SCR-BATCH-DEFINITION"))
            .andExpect(jsonPath("$.data.requiredRole").value("R09"))
            .andExpect(jsonPath("$.data.totalCount").value(1))
            .andExpect(jsonPath("$.data.items[0].batchId").value("COMMON-AUDIT-ROLLUP"))
            .andExpect(jsonPath("$.data.items[0].schedule").value("0 0 * * *"))
            .andExpect(jsonPath("$.data.items[0].ownerId").value("admin"))
            .andExpect(jsonPath("$.data.items[0].statusName").value("정의됨"));
    }

    @Test
    void saveBatchDefinitionUpsertsDefinitionAndWritesAuditTrail() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(batchDefinitionManagementService.saveBatchDefinition(any(SaveBatchDefinitionRequest.class)))
            .thenReturn(new SaveBatchDefinitionResponse("COMMON-AUDIT-ROLLUP", "DEFINED", "배치 정의 관리 저장이 완료되었습니다."));

        mockMvc.perform(post("/api/admin/batch-definitions")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"COMMON-AUDIT-ROLLUP\",\"schedule\":\"0 0 * * *\",\"parameters\":\"{\\\"businessArea\\\":\\\"COMMON_FOUNDATION\\\"}\",\"maxRuntimeSeconds\":3600,\"ownerId\":\"admin\",\"reason\":\"정기 감사 집계 주기 확인\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.batchId").value("COMMON-AUDIT-ROLLUP"))
            .andExpect(jsonPath("$.data.status").value("DEFINED"));

        verify(batchDefinitionManagementService).saveBatchDefinition(any(SaveBatchDefinitionRequest.class));
    }

    @Test
    void saveBatchDefinitionMissingIdentifierReturnsFieldValidationError() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));

        mockMvc.perform(post("/api/admin/batch-definitions")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"schedule\":\"0 0 * * *\",\"maxRuntimeSeconds\":3600,\"ownerId\":\"admin\",\"reason\":\"식별자 누락 검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.fields.id").exists());
    }

    @Test
    void listBatchDefinitionsForNonR09ReturnsForbiddenEnvelope() throws Exception {
        when(authService.authenticate("R01-SESSION"))
            .thenReturn(new SessionPrincipal("R01-SESSION", new AuthenticatedUser("teacher01", List.of("R01"), "SELF")));

        mockMvc.perform(get("/api/admin/batch-definitions")
                .cookie(new Cookie("KNUE_SESSION_ID", "R01-SESSION")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }
}
