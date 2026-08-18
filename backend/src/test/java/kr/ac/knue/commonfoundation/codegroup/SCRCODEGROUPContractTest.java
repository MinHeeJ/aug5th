package kr.ac.knue.commonfoundation.codegroup;

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

@WebMvcTest(controllers = CodeGroupManagementController.class)
@Import({GlobalApiExceptionHandler.class, CurrentUserContext.class, AuthInterceptor.class, WebMvcConfig.class})
class SCRCODEGROUPContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private CodeGroupManagementService codeGroupManagementService;

    @Test
    void openApiFixtureContainsAuthoritativeListCodeGroupsContract() throws Exception {
        ClassPathResource resource = new ClassPathResource("contracts/openapi.yaml");
        String contract = new String(resource.getInputStream().readAllBytes());

        org.assertj.core.api.Assertions.assertThat(contract)
            .contains("operationId: listCodeGroups")
            .contains("/api/admin/code-groups")
            .contains("x-roles:")
            .contains("- R09")
            .contains("code_groups");
    }

    @Test
    void listCodeGroupsReturnsSourceBackedCodeGroupRowsForR09() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(codeGroupManagementService.listCodeGroups(any(CodeGroupSearchCondition.class)))
            .thenReturn(new CodeGroupListResponse(List.of(new CodeGroupListItem(
                "EVAL_AREA",
                "평가영역",
                "교수업적 평가영역 코드 묶음",
                "교수지원과",
                true,
                3,
                3,
                "상세코드 관리에서 코드값·정렬순서 변경"
            )), 1, 20, 1, "SCR-CODE-GROUP", "R09"));

        mockMvc.perform(get("/api/admin/code-groups")
                .param("page", "1")
                .param("size", "20")
                .param("q", "평가")
                .param("filter", "enabled=true;managingDepartment=교수지원과")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.screenId").value("SCR-CODE-GROUP"))
            .andExpect(jsonPath("$.data.requiredRole").value("R09"))
            .andExpect(jsonPath("$.data.totalCount").value(1))
            .andExpect(jsonPath("$.data.items[0].groupId").value("EVAL_AREA"))
            .andExpect(jsonPath("$.data.items[0].groupName").value("평가영역"))
            .andExpect(jsonPath("$.data.items[0].managingDepartment").value("교수지원과"))
            .andExpect(jsonPath("$.data.items[0].detailCount").value(3));
    }

    @Test
    void listCodeGroupsWithoutSessionReturnsUnauthorizedEnvelope() throws Exception {
        when(authService.authenticate(null))
            .thenThrow(new kr.ac.knue.commonfoundation.api.ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));

        mockMvc.perform(get("/api/admin/code-groups"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void listCodeGroupsForNonR09ReturnsForbiddenEnvelope() throws Exception {
        when(authService.authenticate("R01-SESSION"))
            .thenReturn(new SessionPrincipal("R01-SESSION", new AuthenticatedUser("teacher01", List.of("R01"), "SELF")));

        mockMvc.perform(get("/api/admin/code-groups")
                .cookie(new Cookie("KNUE_SESSION_ID", "R01-SESSION")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    void saveCodeGroupUpdatesEditableFieldsAndReturnsSuccessMessage() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(codeGroupManagementService.saveCodeGroup(any(SaveCodeGroupRequest.class)))
            .thenReturn(new SaveCodeGroupResponse("EVAL_AREA", "평가영역", "교수지원과", false, "코드그룹 관리 저장이 완료되었습니다."));

        mockMvc.perform(post("/api/admin/code-groups")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"EVAL_AREA\",\"groupName\":\"평가영역\",\"description\":\"교수업적 평가영역 코드 묶음\",\"managingDepartment\":\"교수지원과\",\"enabled\":false,\"reason\":\"코드그룹 정비\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.groupId").value("EVAL_AREA"))
            .andExpect(jsonPath("$.data.groupName").value("평가영역"))
            .andExpect(jsonPath("$.data.enabled").value(false))
            .andExpect(jsonPath("$.data.message").value("코드그룹 관리 저장이 완료되었습니다."));

        verify(codeGroupManagementService).saveCodeGroup(any(SaveCodeGroupRequest.class));
    }

    @Test
    void saveCodeGroupMissingIdentifierReturnsFieldValidationError() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));

        mockMvc.perform(post("/api/admin/code-groups")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupName\":\"평가영역\",\"description\":\"필수값 검증\",\"managingDepartment\":\"교수지원과\",\"enabled\":true,\"reason\":\"필수값 검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.fields.id").exists());
    }
}
