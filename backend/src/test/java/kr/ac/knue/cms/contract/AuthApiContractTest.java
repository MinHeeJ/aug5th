package kr.ac.knue.cms.contract;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

@SpringBootTest
@AutoConfigureMockMvc
class AuthApiContractTest {
    @Autowired MockMvc mockMvc;

    @Test
    void auth_contract_fixture_contains_login_current_user_and_logout_operations() throws Exception {
        String yaml = new ClassPathResource("contracts/openapi.yaml").getContentAsString(StandardCharsets.UTF_8);
        org.assertj.core.api.Assertions.assertThat(yaml)
            .contains("operationId: login", "operationId: getCurrentUser", "operationId: logout");
    }

    @Test
    void login_with_seed_admin_creates_session_cookie_and_returns_r09_user_envelope() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"admin","password":"admin"}
                    """))
            .andExpect(status().isOk())
            .andExpect(cookie().httpOnly("SESSION", true))
            .andExpect(header().string("Set-Cookie", containsString("SameSite=Lax")))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.meta.timestamp").exists())
            .andExpect(jsonPath("$.data.loginId").value("admin"))
            .andExpect(jsonPath("$.data.staffName").value("시스템 관리자"))
            .andExpect(jsonPath("$.data.roleCodes", containsInAnyOrder("R09")));
    }

    @Test
    void login_validation_error_returns_field_errors_without_session_cookie() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(cookie().doesNotExist("SESSION"))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.fields.username").exists())
            .andExpect(jsonPath("$.error.fields.password").exists());
    }

    @Test
    void current_user_requires_session_and_returns_authenticated_user_when_cookie_is_valid() throws Exception {
        Cookie session = loginAsAdmin();

        mockMvc.perform(get("/api/auth/me").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.loginId").value("admin"))
            .andExpect(jsonPath("$.data.roleCodes[0]").value("R09"));

        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void logout_marks_session_inactive_and_current_user_after_logout_is_unauthorized() throws Exception {
        Cookie session = loginAsAdmin();

        mockMvc.perform(post("/api/auth/logout").cookie(session))
            .andExpect(status().isOk())
            .andExpect(cookie().maxAge("SESSION", 0))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("로그아웃되었습니다."));

        mockMvc.perform(get("/api/auth/me").cookie(session))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
    }

    private Cookie loginAsAdmin() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"admin","password":"admin"}
                    """))
            .andExpect(status().isOk())
            .andReturn();
        MockHttpServletResponse response = result.getResponse();
        return response.getCookie("SESSION");
    }
}
