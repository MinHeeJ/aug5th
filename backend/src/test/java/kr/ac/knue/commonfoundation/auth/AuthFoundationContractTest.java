package kr.ac.knue.commonfoundation.auth;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import jakarta.servlet.http.Cookie;
import kr.ac.knue.commonfoundation.api.ApiException;
import kr.ac.knue.commonfoundation.api.GlobalApiExceptionHandler;
import kr.ac.knue.commonfoundation.config.WebMvcConfig;
import kr.ac.knue.commonfoundation.foundation.FoundationAccessController;
import kr.ac.knue.commonfoundation.foundation.FoundationAccessService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {AuthController.class, FoundationAccessController.class})
@Import({GlobalApiExceptionHandler.class, CurrentUserContext.class, AuthInterceptor.class, WebMvcConfig.class})
class AuthFoundationContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private FoundationAccessService foundationAccessService;

    @Test
    void loginWithAdminCreatesSessionCookieAndReturnsR09DataScope() throws Exception {
        when(authService.login(any(LoginRequest.class), any()))
            .thenReturn(new SessionPrincipal("SESSION-1", new AuthenticatedUser("admin", List.of("R09"), "ALL")));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"admin\",\"password\":\"admin\"}"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("KNUE_SESSION_ID=SESSION-1")))
            .andExpect(cookie().httpOnly("KNUE_SESSION_ID", true))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.userId").value("admin"))
            .andExpect(jsonPath("$.data.roles[0]").value("R09"))
            .andExpect(jsonPath("$.data.dataScope").value("ALL"));
    }

    @Test
    void loginValidationErrorReturnsPasswordFieldError() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"admin\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.fields.password").exists());
    }

    @Test
    void invalidPasswordReturnsUnauthorizedApiErrorWithoutSessionCookie() throws Exception {
        when(authService.login(any(LoginRequest.class), any()))
            .thenThrow(new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"admin\",\"password\":\"wrong\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void protectedApiWithoutSessionCookieReturnsUnauthorized() throws Exception {
        when(authService.authenticate(null))
            .thenThrow(new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."));

        mockMvc.perform(get("/api/auth/session"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void adminFoundationEndpointEnforcesR09Authorization() throws Exception {
        when(authService.authenticate("R01-SESSION"))
            .thenReturn(new SessionPrincipal("R01-SESSION", new AuthenticatedUser("teacher", List.of("R01"), "SELF")));

        mockMvc.perform(get("/api/admin/foundation/data-scope")
                .cookie(new Cookie("KNUE_SESSION_ID", "R01-SESSION")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    @Test
    void adminFoundationEndpointReturnsEnforcedDataScopeForR09() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));
        when(foundationAccessService.currentDataScope())
            .thenReturn(Map.of(
                "userId", "admin",
                "roles", List.of("R09"),
                "dataScope", "ALL",
                "enforced", true,
                "permissionRows", 4L
            ));

        mockMvc.perform(get("/api/admin/foundation/data-scope")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.userId").value("admin"))
            .andExpect(jsonPath("$.data.dataScope").value("ALL"))
            .andExpect(jsonPath("$.data.permissionRows").value(4))
            .andExpect(jsonPath("$.data.enforced").value(true));

        verify(foundationAccessService).currentDataScope();
    }

    @Test
    void logoutRequiresActiveSessionAndDelegatesStateTransition() throws Exception {
        when(authService.authenticate("ADMIN-SESSION"))
            .thenReturn(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL")));

        mockMvc.perform(post("/api/auth/logout")
                .cookie(new Cookie("KNUE_SESSION_ID", "ADMIN-SESSION")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("LOGOUT"));

        verify(authService).logout(eq(new SessionPrincipal("ADMIN-SESSION", new AuthenticatedUser("admin", List.of("R09"), "ALL"))));
    }
}
