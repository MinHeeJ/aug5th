package kr.ac.knue.cms.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class NavigationPermissionIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    void navigation_requires_authenticated_session() throws Exception {
        mockMvc.perform(get("/api/navigation/menus"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void admin_navigation_returns_nine_leaf_menus_in_database_order() throws Exception {
        Cookie admin = login("admin", "admin");

        mockMvc.perform(get("/api/navigation/menus").cookie(admin))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].menuName").value("시스템 관리"))
            .andExpect(jsonPath("$.data[0].children", hasSize(4)))
            .andExpect(jsonPath("$.data[0].children[0].menuName").value("사용자·조직"))
            .andExpect(jsonPath("$.data[0].children[0].children[*].menuName", contains("사용자 관리", "조직 관리")))
            .andExpect(jsonPath("$.data[0].children[1].children[*].menuName", contains("역할 관리", "사용자 역할 관리", "메뉴 권한 관리")))
            .andExpect(jsonPath("$.data[0].children[2].children[*].menuName", contains("메뉴 구조 관리", "메뉴 정보 관리")))
            .andExpect(jsonPath("$.data[0].children[3].children[*].menuName", contains("코드그룹 관리", "상세코드 관리")));
    }

    @Test
    void navigation_filters_out_leaf_menus_disallowed_for_r09_without_reordering_allowed_menus() throws Exception {
        jdbcTemplate.update("""
            update menu_permissions
            set is_allowed = false, updated_at = now()
            where target_type = 'ROLE' and target_id = 'R09'
              and menu_id = '00000000-0000-0000-0000-000000000205'
            """);
        try {
            Cookie admin = login("admin", "admin");

            MvcResult result = mockMvc.perform(get("/api/navigation/menus").cookie(admin))
                .andExpect(status().isOk())
                .andReturn();
            String body = result.getResponse().getContentAsString();
            assertThat(body).doesNotContain("메뉴 권한 관리");
            assertThat(body).contains("역할 관리", "사용자 역할 관리", "메뉴 구조 관리");
        } finally {
            jdbcTemplate.update("""
                update menu_permissions
                set is_allowed = true, updated_at = now()
                where target_type = 'ROLE' and target_id = 'R09'
                  and menu_id = '00000000-0000-0000-0000-000000000205'
                """);
        }
    }

    @Test
    void non_r09_authenticated_user_cannot_load_admin_navigation() throws Exception {
        Cookie faculty = login("faculty", "faculty");

        mockMvc.perform(get("/api/navigation/menus").cookie(faculty))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));
    }

    private Cookie login(String username, String password) throws Exception {
        String body = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        MvcResult result = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andReturn();
        MockHttpServletResponse response = result.getResponse();
        return response.getCookie("SESSION");
    }
}
