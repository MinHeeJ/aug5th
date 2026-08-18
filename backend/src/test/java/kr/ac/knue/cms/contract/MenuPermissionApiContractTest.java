package kr.ac.knue.cms.contract;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class MenuPermissionApiContractTest {
    @Autowired MockMvc mockMvc;

    @Test
    void openapi_fixture_declares_menu_permission_operations() throws Exception {
        String yaml = new ClassPathResource("contracts/openapi.yaml").getContentAsString(StandardCharsets.UTF_8);
        org.assertj.core.api.Assertions.assertThat(yaml)
            .contains("/api/admin/menu-permissions", "/api/admin/menu-permissions/{targetType}/{targetId}",
                "/api/admin/menu-permissions/effective", "MenuPermissionUpdateRequest");
    }

    @Test
    void list_menu_permissions_returns_target_matrix_with_menu_rows() throws Exception {
        Cookie session = loginAsAdmin();

        mockMvc.perform(get("/api/admin/menu-permissions").cookie(session)
                .param("targetType", "ROLE")
                .param("targetId", "R09"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[*].targetType", hasItem("ROLE")))
            .andExpect(jsonPath("$.data[*].targetId", hasItem("R09")))
            .andExpect(jsonPath("$.data[*].menuName", hasItem("메뉴 권한 관리")))
            .andExpect(jsonPath("$.data[*].isAllowed", hasItem(true)));
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = {
        "update menu_permissions set is_allowed = true, is_used = true, updated_at = now() where target_type = 'ROLE' and target_id = 'R09'"
    })
    void save_menu_permissions_persists_target_allowance_and_effective_result() throws Exception {
        Cookie session = loginAsAdmin();
        String menuId = "00000000-0000-0000-0000-000000000130";

        mockMvc.perform(put("/api/admin/menu-permissions/ROLE/R09")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"permissions":[{"targetType":"ROLE","targetId":"R09","menuId":"00000000-0000-0000-0000-000000000130","isAllowed":false}]}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[?(@.menuId=='" + menuId + "')].isAllowed", hasItem(false)));

        mockMvc.perform(get("/api/admin/menu-permissions").cookie(session)
                .param("targetType", "ROLE")
                .param("targetId", "R09"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[?(@.menuId=='" + menuId + "')].isAllowed", hasItem(false)));

        mockMvc.perform(get("/api/admin/menu-permissions/effective").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[*].menuId").exists());
    }

    @Test
    void save_menu_permissions_rejects_invalid_target_type_as_api_error_fields() throws Exception {
        Cookie session = loginAsAdmin();

        mockMvc.perform(put("/api/admin/menu-permissions/GROUP/R09")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"permissions":[{"targetType":"GROUP","targetId":"R09","menuId":"00000000-0000-0000-0000-000000000207","isAllowed":true}]}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.fields", hasKey("targetType")));
    }

    private Cookie loginAsAdmin() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"admin\"}"))
            .andExpect(status().isOk())
            .andExpect(cookie().exists("SESSION"))
            .andReturn();
        return result.getResponse().getCookie("SESSION");
    }
}
