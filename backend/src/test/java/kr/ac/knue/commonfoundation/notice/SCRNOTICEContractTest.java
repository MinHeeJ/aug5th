package kr.ac.knue.commonfoundation.notice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
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

@WebMvcTest(controllers = NoticeManagementController.class)
@Import({GlobalApiExceptionHandler.class, CurrentUserContext.class, AuthInterceptor.class, WebMvcConfig.class})
class SCRNOTICEContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private NoticeManagementService noticeManagementService;

    @Test
    void openApiFixtureContainsAuthoritativeListNoticesContract() throws Exception {
        ClassPathResource resource = new ClassPathResource("contracts/openapi.yaml");
        String contract = new String(resource.getInputStream().readAllBytes());

        org.assertj.core.api.Assertions.assertThat(contract)
            .contains("operationId: listNotices")
            .contains("/api/admin/notices")
            .contains("x-roles:")
            .contains("- R09")
            .contains("REQ-052")
            .contains("REQ-053")
            .contains("REQ-054")
            .contains("REQ-055");
    }

    @Test
    void listNoticesReturnsPublicationTargetAndPeriodRowsForR09() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(noticeManagementService.listNotices(any(NoticeSearchCondition.class)))
            .thenReturn(new NoticeListResponse(List.of(new NoticeListItem(
                1L,
                "2026학년도 교수업적평가 공통 일정 안내",
                "평가일정과 시스템 점검 기간을 확인하세요.",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                "R01,R09",
                "KNUE-EDU",
                true,
                true,
                0,
                "지정 대상 역할·조직과 게시기간에만 노출됩니다.",
                "공지 열람은 업무 승인이나 확인처리로 간주하지 않습니다."
            )), 1, 20, 1, "SCR-NOTICE", "R09"));

        mockMvc.perform(get("/api/admin/notices")
                .param("page", "1")
                .param("size", "20")
                .param("q", "평가")
                .param("filter", "targetRole=R09;important=true;enabled=true;activeOn=2026-08-16")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.screenId").value("SCR-NOTICE"))
            .andExpect(jsonPath("$.data.requiredRole").value("R09"))
            .andExpect(jsonPath("$.data.totalCount").value(1))
            .andExpect(jsonPath("$.data.items[0].noticeId").value(1))
            .andExpect(jsonPath("$.data.items[0].title").value("2026학년도 교수업적평가 공통 일정 안내"))
            .andExpect(jsonPath("$.data.items[0].targetRoles").value("R01,R09"))
            .andExpect(jsonPath("$.data.items[0].targetOrganizations").value("KNUE-EDU"))
            .andExpect(jsonPath("$.data.items[0].important").value(true))
            .andExpect(jsonPath("$.data.items[0].exposureRule").value("지정 대상 역할·조직과 게시기간에만 노출됩니다."));
    }

    @Test
    void listNoticesWithoutSessionReturnsUnauthorizedEnvelope() throws Exception {
        when(authService.authenticate(null))
            .thenThrow(new kr.ac.knue.commonfoundation.api.ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));

        mockMvc.perform(get("/api/admin/notices"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void listNoticesForNonR09ReturnsForbiddenEnvelope() throws Exception {
        when(authService.authenticate("R01-SESSION"))
            .thenReturn(new SessionPrincipal("R01-SESSION", new AuthenticatedUser("teacher01", List.of("R01"), "SELF")));

        mockMvc.perform(get("/api/admin/notices")
                .cookie(new Cookie("KNUE_SESSION_ID", "R01-SESSION")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    void saveNoticeUpdatesPublicationWindowAndReturnsSuccessMessage() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(noticeManagementService.saveNotice(any(SaveNoticeRequest.class)))
            .thenReturn(new SaveNoticeResponse(1L, "2026학년도 교수업적평가 공통 일정 안내", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), "R01,R09", "KNUE-EDU", true, true, "공지사항 관리 저장이 완료되었습니다."));

        mockMvc.perform(post("/api/admin/notices")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"1\",\"title\":\"2026학년도 교수업적평가 공통 일정 안내\",\"postFrom\":\"2026-01-01\",\"postTo\":\"2026-12-31\",\"targetRoles\":\"R01,R09\",\"targetOrganizations\":\"KNUE-EDU\",\"important\":true,\"enabled\":true,\"reason\":\"게시 대상과 기간 조정\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.noticeId").value(1))
            .andExpect(jsonPath("$.data.targetRoles").value("R01,R09"))
            .andExpect(jsonPath("$.data.important").value(true))
            .andExpect(jsonPath("$.data.message").value("공지사항 관리 저장이 완료되었습니다."));

        verify(noticeManagementService).saveNotice(any(SaveNoticeRequest.class));
    }

    @Test
    void saveNoticeMissingIdentifierReturnsFieldValidationError() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));

        mockMvc.perform(post("/api/admin/notices")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"공지\",\"postFrom\":\"2026-01-01\",\"postTo\":\"2026-12-31\",\"targetRoles\":\"R09\",\"important\":true,\"enabled\":true,\"reason\":\"필수값 검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.fields.id").exists());
    }
}
