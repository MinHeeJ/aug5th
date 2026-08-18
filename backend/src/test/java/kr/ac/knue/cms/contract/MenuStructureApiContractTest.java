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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class MenuStructureApiContractTest {
    @Autowired MockMvc mockMvc;

    @Test
    void openapi_fixture_declares_menu_structure_operations() throws Exception {
        String yaml = new ClassPathResource("contracts/openapi.yaml").getContentAsString(StandardCharsets.UTF_8);
        org.assertj.core.api.Assertions.assertThat(yaml)
            .contains("/api/admin/menu-structure", "/api/admin/menu-structure/{menuId}",
                "/api/admin/menu-structure/reorder", "MenuReorderRequest");
    }

    @Test
    void get_menu_structure_returns_hierarchy_fields_in_defined_order() throws Exception {
        Cookie session = loginAsAdmin();

        mockMvc.perform(get("/api/admin/menu-structure").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[*].menuLevel", hasItem("MAIN")))
            .andExpect(jsonPath("$.data[*].menuName", hasItem("메뉴 구조 관리")))
            .andExpect(jsonPath("$.data[*].displayOrder").exists());
    }

    @Test
    void save_menu_structure_updates_parent_and_order_then_navigation_reflects_order() throws Exception {
        Cookie session = loginAsAdmin();

        mockMvc.perform(put("/api/admin/menu-structure/00000000-0000-0000-0000-000000000207")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"menuId":"00000000-0000-0000-0000-000000000207","parentMenuId":"00000000-0000-0000-0000-000000000130","menuLevel":"SUB","displayOrder":3,"menuName":"메뉴 정보 관리","screenId":"SCR-MENU-INFO","url":"/api/admin/menus","icon":"layout","businessDivision":"공통","description":"메뉴 실행정보 관리","isUsed":true}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.menuId").value("00000000-0000-0000-0000-000000000207"))
            .andExpect(jsonPath("$.data.parentMenuId").value("00000000-0000-0000-0000-000000000130"))
            .andExpect(jsonPath("$.data.displayOrder").value(3));

        mockMvc.perform(put("/api/admin/menu-structure/reorder")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"parentMenuId":"00000000-0000-0000-0000-000000000130","orderedMenuIds":["00000000-0000-0000-0000-000000000206","00000000-0000-0000-0000-000000000207"]}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].displayOrder").value(1));

        mockMvc.perform(get("/api/navigation/menus").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[*].menuName").exists());
    }

    @Test
    void reorder_rejects_cross_parent_menu_ids() throws Exception {
        Cookie session = loginAsAdmin();

        mockMvc.perform(put("/api/admin/menu-structure/reorder")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"parentMenuId":"00000000-0000-0000-0000-000000000130","orderedMenuIds":["00000000-0000-0000-0000-000000000201","00000000-0000-0000-0000-000000000207"]}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fields", hasKey("orderedMenuIds")));
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
