package kr.ac.knue.cms.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AllAdminReadApisSmokeTest {
    @Autowired MockMvc mockMvc;

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("adminReadApis")
    @DisplayName("시드 관리자 세션으로 9개 관리 화면 조회 API가 모두 2xx envelope를 반환한다")
    void seed_admin_session_can_read_all_nine_admin_screen_apis(String screenName, String path) throws Exception {
        mockMvc.perform(get(path).cookie(adminSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.meta.timestamp", not(nullValue())))
            .andExpect(jsonPath("$.data", not(nullValue())));
    }

    @Test
    void seed_admin_navigation_contains_nine_screen_links_before_read_smoke() throws Exception {
        String body = mockMvc.perform(get("/api/navigation/menus").cookie(adminSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThat(body).contains(
            "/admin/users",
            "/admin/organizations",
            "/admin/roles",
            "/admin/user-roles",
            "/admin/menu-permissions",
            "/admin/menu-structure",
            "/admin/menus",
            "/admin/code-groups",
            "/admin/code-groups/COMMON/codes"
        );
    }

    @Test
    void protected_admin_read_api_without_session_is_rejected_with_error_envelope() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
    }

    private static Stream<Arguments> adminReadApis() {
        return Stream.of(
            Arguments.of("사용자 관리", "/api/admin/users"),
            Arguments.of("조직 관리", "/api/admin/organizations"),
            Arguments.of("역할 관리", "/api/admin/roles"),
            Arguments.of("사용자 역할 관리", "/api/admin/user-roles"),
            Arguments.of("메뉴 권한 관리", "/api/admin/menu-permissions?targetType=ROLE&targetId=R09"),
            Arguments.of("메뉴 구조 관리", "/api/admin/menu-structure"),
            Arguments.of("메뉴 정보 관리", "/api/admin/menus"),
            Arguments.of("코드그룹 관리", "/api/admin/code-groups"),
            Arguments.of("상세코드 관리", "/api/admin/code-groups/COMMON/codes")
        );
    }

    private Cookie adminSession() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"admin\"}"))
            .andExpect(status().isOk())
            .andReturn();
        return result.getResponse().getCookie("SESSION");
    }
}
