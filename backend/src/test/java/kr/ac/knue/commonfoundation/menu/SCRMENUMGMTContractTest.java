package kr.ac.knue.commonfoundation.menu;

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

@WebMvcTest(controllers = MenuManagementController.class)
@Import({GlobalApiExceptionHandler.class, CurrentUserContext.class, AuthInterceptor.class, WebMvcConfig.class})
class SCRMENUMGMTContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private MenuManagementService menuManagementService;

    @Test
    void openApiFixtureContainsAuthoritativeListMenusContract() throws Exception {
        ClassPathResource resource = new ClassPathResource("contracts/openapi.yaml");
        String contract = new String(resource.getInputStream().readAllBytes());

        org.assertj.core.api.Assertions.assertThat(contract)
            .contains("operationId: listMenus")
            .contains("/api/admin/menus")
            .contains("x-roles:")
            .contains("- R09")
            .contains("menus");
    }

    @Test
    void listMenusReturnsMenuTreeForR09() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(menuManagementService.listMenus(any(MenuSearchCondition.class)))
            .thenReturn(new MenuListResponse(List.of(new MenuListItem(
                "M-MENU-MGMT",
                "M-SYSTEM",
                "시스템 관리",
                "메뉴 관리",
                "SCR-MENU-MGMT",
                "/admin/menus",
                18,
                0,
                9,
                "화면 표시 및 서버 메뉴 권한 판정 기준"
            )), 1, 20, 1, "SCR-MENU-MGMT", "R09"));

        mockMvc.perform(get("/api/admin/menus")
                .param("page", "1")
                .param("size", "20")
                .param("q", "메뉴")
                .param("filter", "parentMenuId=M-SYSTEM;screenId=SCR-MENU-MGMT")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.screenId").value("SCR-MENU-MGMT"))
            .andExpect(jsonPath("$.data.requiredRole").value("R09"))
            .andExpect(jsonPath("$.data.totalCount").value(1))
            .andExpect(jsonPath("$.data.items[0].menuId").value("M-MENU-MGMT"))
            .andExpect(jsonPath("$.data.items[0].parentMenuName").value("시스템 관리"))
            .andExpect(jsonPath("$.data.items[0].screenId").value("SCR-MENU-MGMT"))
            .andExpect(jsonPath("$.data.items[0].url").value("/admin/menus"));
    }

    @Test
    void listMenusWithoutSessionReturnsUnauthorizedEnvelope() throws Exception {
        when(authService.authenticate(null))
            .thenThrow(new kr.ac.knue.commonfoundation.api.ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));

        mockMvc.perform(get("/api/admin/menus"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void listMenusForNonR09ReturnsForbiddenEnvelope() throws Exception {
        when(authService.authenticate("R01-SESSION"))
            .thenReturn(new SessionPrincipal("R01-SESSION", new AuthenticatedUser("teacher01", List.of("R01"), "SELF")));

        mockMvc.perform(get("/api/admin/menus")
                .cookie(new Cookie("KNUE_SESSION_ID", "R01-SESSION")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    void saveMenuUpdatesEditableFieldsAndReturnsSuccessMessage() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(menuManagementService.saveMenu(any(SaveMenuRequest.class)))
            .thenReturn(new SaveMenuResponse("M-MENU-MGMT", "메뉴 관리", "SCR-MENU-MGMT", "/admin/menus", 18, "메뉴 관리 저장이 완료되었습니다."));

        mockMvc.perform(post("/api/admin/menus")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"M-MENU-MGMT\",\"parentMenuId\":\"M-SYSTEM\",\"menuName\":\"메뉴 관리\",\"screenId\":\"SCR-MENU-MGMT\",\"url\":\"/admin/menus\",\"displayOrder\":18,\"reason\":\"메뉴 트리 점검\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.menuId").value("M-MENU-MGMT"))
            .andExpect(jsonPath("$.data.menuName").value("메뉴 관리"))
            .andExpect(jsonPath("$.data.screenId").value("SCR-MENU-MGMT"))
            .andExpect(jsonPath("$.data.message").value("메뉴 관리 저장이 완료되었습니다."));

        verify(menuManagementService).saveMenu(any(SaveMenuRequest.class));
    }

    @Test
    void saveMenuMissingIdentifierReturnsFieldValidationError() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));

        mockMvc.perform(post("/api/admin/menus")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"menuName\":\"메뉴 관리\",\"screenId\":\"SCR-MENU-MGMT\",\"url\":\"/admin/menus\",\"displayOrder\":18,\"reason\":\"필수값 검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.fields.id").exists());
    }
}
