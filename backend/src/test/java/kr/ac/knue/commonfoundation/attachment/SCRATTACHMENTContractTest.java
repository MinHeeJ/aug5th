package kr.ac.knue.commonfoundation.attachment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AttachmentManagementController.class)
@Import({GlobalApiExceptionHandler.class, CurrentUserContext.class, AuthInterceptor.class, WebMvcConfig.class})
class SCRATTACHMENTContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private AttachmentManagementService attachmentManagementService;

    @Test
    void openApiFixtureContainsAuthoritativeListAttachmentsContract() throws Exception {
        ClassPathResource resource = new ClassPathResource("contracts/openapi.yaml");
        String contract = new String(resource.getInputStream().readAllBytes());

        org.assertj.core.api.Assertions.assertThat(contract)
            .contains("operationId: listAttachments")
            .contains("/api/admin/attachments")
            .contains("x-roles:")
            .contains("- R09")
            .contains("REQ-056")
            .contains("REQ-057")
            .contains("REQ-058")
            .contains("REQ-059")
            .contains("REQ-060")
            .contains("REQ-061")
            .contains("REQ-062");
    }

    @Test
    void listAttachmentsReturnsFileMetadataIntegrityRowsForR09() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(attachmentManagementService.listAttachments(any(AttachmentSearchCondition.class)))
            .thenReturn(new AttachmentListResponse(List.of(new AttachmentListItem(
                1L,
                "NOTICE:1",
                "평가일정 안내.pdf",
                "2026/08/notice-1.pdf",
                "pdf",
                204800L,
                "admin",
                LocalDateTime.of(2026, 8, 16, 9, 30),
                "CLEAN",
                false,
                false,
                true,
                "OK",
                "다운로드 시 권한을 재검증합니다.",
                "개발·검증 환경에서는 논리삭제만 허용합니다."
            )), 1, 20, 1, "SCR-ATTACHMENT", "R09"));

        mockMvc.perform(get("/api/admin/attachments")
                .param("page", "1")
                .param("size", "20")
                .param("q", "평가")
                .param("filter", "businessKey=NOTICE:1;malwareScanResult=CLEAN;deleted=false;integrityStatus=OK")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.screenId").value("SCR-ATTACHMENT"))
            .andExpect(jsonPath("$.data.requiredRole").value("R09"))
            .andExpect(jsonPath("$.data.totalCount").value(1))
            .andExpect(jsonPath("$.data.items[0].attachmentId").value(1))
            .andExpect(jsonPath("$.data.items[0].businessKey").value("NOTICE:1"))
            .andExpect(jsonPath("$.data.items[0].originalName").value("평가일정 안내.pdf"))
            .andExpect(jsonPath("$.data.items[0].storedName").value("2026/08/notice-1.pdf"))
            .andExpect(jsonPath("$.data.items[0].malwareScanResult").value("CLEAN"))
            .andExpect(jsonPath("$.data.items[0].integrityStatus").value("OK"))
            .andExpect(jsonPath("$.data.items[0].downloadAuthorizationRule").value("다운로드 시 권한을 재검증합니다."));
    }

    @Test
    void listAttachmentsWithoutSessionReturnsUnauthorizedEnvelope() throws Exception {
        when(authService.authenticate(null))
            .thenThrow(new kr.ac.knue.commonfoundation.api.ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));

        mockMvc.perform(get("/api/admin/attachments"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void listAttachmentsForNonR09ReturnsForbiddenEnvelope() throws Exception {
        when(authService.authenticate("R01-SESSION"))
            .thenReturn(new SessionPrincipal("R01-SESSION", new AuthenticatedUser("teacher01", List.of("R01"), "SELF")));

        mockMvc.perform(get("/api/admin/attachments")
                .cookie(new Cookie("KNUE_SESSION_ID", "R01-SESSION")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    void saveAttachmentLogicalDeleteReturnsSuccessMessage() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(attachmentManagementService.saveAttachment(any(SaveAttachmentRequest.class)))
            .thenReturn(new SaveAttachmentResponse(1L, "NOTICE:1", "평가일정 안내.pdf", true, "LOGICAL_DELETE", "첨부파일 관리 저장이 완료되었습니다."));

        mockMvc.perform(post("/api/admin/attachments")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"1\",\"businessKey\":\"NOTICE:1\",\"deleteRequested\":true,\"deleteReason\":\"중복 첨부 정리\",\"reason\":\"첨부파일 논리삭제 확인\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.attachmentId").value(1))
            .andExpect(jsonPath("$.data.deleted").value(true))
            .andExpect(jsonPath("$.data.actionResult").value("LOGICAL_DELETE"))
            .andExpect(jsonPath("$.data.message").value("첨부파일 관리 저장이 완료되었습니다."));

        verify(attachmentManagementService).saveAttachment(any(SaveAttachmentRequest.class));
    }

    @Test
    void saveAttachmentMissingIdentifierReturnsFieldValidationError() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));

        mockMvc.perform(post("/api/admin/attachments")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"businessKey\":\"NOTICE:1\",\"deleteRequested\":true,\"deleteReason\":\"중복 첨부 정리\",\"reason\":\"필수값 검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.fields.id").exists());
    }

    @Test
    void deleteAttachmentWritesLogicalDeleteSideEffectAndReturnsAttachmentState() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(attachmentManagementService.deleteAttachment(eq(1L), any(DeleteAttachmentRequest.class)))
            .thenReturn(new SaveAttachmentResponse(1L, "NOTICE:1", "평가일정 안내.pdf", true, "LOGICAL_DELETE", "첨부파일 삭제 요청이 기록되었습니다."));

        mockMvc.perform(post("/api/admin/attachments/1/delete")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"오등록 첨부 논리삭제\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.attachmentId").value(1))
            .andExpect(jsonPath("$.data.businessKey").value("NOTICE:1"))
            .andExpect(jsonPath("$.data.deleted").value(true))
            .andExpect(jsonPath("$.data.actionResult").value("LOGICAL_DELETE"));

        verify(attachmentManagementService).deleteAttachment(eq(1L), any(DeleteAttachmentRequest.class));
    }

    @Test
    void runAttachmentIntegrityChecksReturnsCalculatedIntegrityCounts() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(attachmentManagementService.runIntegrityCheck())
            .thenReturn(new AttachmentIntegrityCheckResponse(12, 1, "WARN", "첨부파일 무결성 점검에서 1건의 이상 항목을 확인했습니다."));

        mockMvc.perform(post("/api/admin/attachments/integrity-checks")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalCount").value(12))
            .andExpect(jsonPath("$.data.abnormalCount").value(1))
            .andExpect(jsonPath("$.data.status").value("WARN"));

        verify(attachmentManagementService).runIntegrityCheck();
    }
}
