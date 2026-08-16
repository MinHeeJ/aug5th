package kr.ac.knue.commonfoundation.codedetail;

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

@WebMvcTest(controllers = CodeDetailManagementController.class)
@Import({GlobalApiExceptionHandler.class, CurrentUserContext.class, AuthInterceptor.class, WebMvcConfig.class})
class SCRCODEDETAILContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private CodeDetailManagementService codeDetailManagementService;

    @Test
    void openApiFixtureContainsAuthoritativeListCodeDetailsContract() throws Exception {
        ClassPathResource resource = new ClassPathResource("contracts/openapi.yaml");
        String contract = new String(resource.getInputStream().readAllBytes());

        org.assertj.core.api.Assertions.assertThat(contract)
            .contains("operationId: listCodeDetails")
            .contains("/api/admin/code-details")
            .contains("x-roles:")
            .contains("- R09")
            .contains("code_details");
    }

    @Test
    void listCodeDetailsReturnsSourceBackedDetailRowsForR09() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(codeDetailManagementService.listCodeDetails(any(CodeDetailSearchCondition.class)))
            .thenReturn(new CodeDetailListResponse(List.of(new CodeDetailListItem(
                1L,
                "EVAL_AREA",
                "평가영역",
                "TEACHING",
                "교육영역",
                null,
                null,
                10,
                true,
                "그룹 내 코드값은 중복될 수 없고 정렬순서로 표시됩니다."
            )), 1, 20, 1, "SCR-CODE-DETAIL", "R09"));

        mockMvc.perform(get("/api/admin/code-details")
                .param("page", "1")
                .param("size", "20")
                .param("q", "교육")
                .param("filter", "groupId=EVAL_AREA;active=true")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.screenId").value("SCR-CODE-DETAIL"))
            .andExpect(jsonPath("$.data.requiredRole").value("R09"))
            .andExpect(jsonPath("$.data.totalCount").value(1))
            .andExpect(jsonPath("$.data.items[0].codeDetailId").value(1))
            .andExpect(jsonPath("$.data.items[0].groupId").value("EVAL_AREA"))
            .andExpect(jsonPath("$.data.items[0].groupName").value("평가영역"))
            .andExpect(jsonPath("$.data.items[0].codeValue").value("TEACHING"))
            .andExpect(jsonPath("$.data.items[0].codeName").value("교육영역"))
            .andExpect(jsonPath("$.data.items[0].active").value(true));
    }

    @Test
    void listCodeDetailsWithoutSessionReturnsUnauthorizedEnvelope() throws Exception {
        when(authService.authenticate(null))
            .thenThrow(new kr.ac.knue.commonfoundation.api.ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));

        mockMvc.perform(get("/api/admin/code-details"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void listCodeDetailsForNonR09ReturnsForbiddenEnvelope() throws Exception {
        when(authService.authenticate("R01-SESSION"))
            .thenReturn(new SessionPrincipal("R01-SESSION", new AuthenticatedUser("teacher01", List.of("R01"), "SELF")));

        mockMvc.perform(get("/api/admin/code-details")
                .cookie(new Cookie("KNUE_SESSION_ID", "R01-SESSION")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    void saveCodeDetailUpdatesEditableFieldsAndReturnsSuccessMessage() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(codeDetailManagementService.saveCodeDetail(any(SaveCodeDetailRequest.class)))
            .thenReturn(new SaveCodeDetailResponse(1L, "EVAL_AREA", "TEACHING", "교육영역", 20, true, "상세코드 관리 저장이 완료되었습니다."));

        mockMvc.perform(post("/api/admin/code-details")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"1\",\"codeName\":\"교육영역\",\"parentCodeValue\":null,\"displayOrder\":20,\"reason\":\"상세코드 정렬 변경\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.codeDetailId").value(1))
            .andExpect(jsonPath("$.data.groupId").value("EVAL_AREA"))
            .andExpect(jsonPath("$.data.codeValue").value("TEACHING"))
            .andExpect(jsonPath("$.data.displayOrder").value(20))
            .andExpect(jsonPath("$.data.message").value("상세코드 관리 저장이 완료되었습니다."));

        verify(codeDetailManagementService).saveCodeDetail(any(SaveCodeDetailRequest.class));
    }

    @Test
    void saveCodeDetailMissingIdentifierReturnsFieldValidationError() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));

        mockMvc.perform(post("/api/admin/code-details")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"codeName\":\"교육영역\",\"displayOrder\":10,\"reason\":\"필수값 검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.fields.id").exists());
    }
}
