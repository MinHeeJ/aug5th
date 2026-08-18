package kr.ac.knue.cms.contract;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
class MenuInformationApiContractTest {
    @Autowired MockMvc mockMvc;

    @Test
    void openapi_fixture_declares_menu_information_operations() throws Exception {
        String yaml = new ClassPathResource("contracts/openapi.yaml").getContentAsString(StandardCharsets.UTF_8);
        org.assertj.core.api.Assertions.assertThat(yaml)
            .contains("/api/admin/menus", "/api/admin/menus/{menuId}", "/api/admin/menus/{menuId}/status", "MenuStatusRequest");
    }

    @Test
    void list_menus_returns_execution_information_fields() throws Exception {
        Cookie session = loginAsAdmin();

        mockMvc.perform(get("/api/admin/menus").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[*].menuName", hasItem("메뉴 정보 관리")))
            .andExpect(jsonPath("$.data[*].screenId", hasItem("SCR-MENU-INFO")))
            .andExpect(jsonPath("$.data[*].url", hasItem("/admin/menus")));
    }

    @Test
    void save_menu_persists_execution_information_and_connection_preview_fields() throws Exception {
        Cookie session = loginAsAdmin();

        mockMvc.perform(put("/api/admin/menus/00000000-0000-0000-0000-000000000207")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"menuId":"00000000-0000-0000-0000-000000000207","parentMenuId":"00000000-0000-0000-0000-000000000130","menuLevel":"SUB","displayOrder":2,"menuName":"메뉴 정보 관리","screenId":"SCR-MENU-INFO","url":"/api/admin/menus","icon":"menu-link","businessDivision":"시스템 관리","description":"메뉴별 실행정보와 화면 연결 관리","isUsed":true}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.menuName").value("메뉴 정보 관리"))
            .andExpect(jsonPath("$.data.screenId").value("SCR-MENU-INFO"))
            .andExpect(jsonPath("$.data.url").value("/admin/menus"));
    }

    @Test
    void update_menu_status_uses_inactivation_not_physical_delete() throws Exception {
        Cookie session = loginAsAdmin();

        mockMvc.perform(patch("/api/admin/menus/00000000-0000-0000-0000-000000000207/status")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"isUsed\":true,\"changeReason\":\"계약 테스트 활성 상태 유지\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.menuId").value("00000000-0000-0000-0000-000000000207"))
            .andExpect(jsonPath("$.data.isUsed").value(true));
    }

    @Test
    void save_menu_rejects_blank_menu_name_as_api_error_fields() throws Exception {
        Cookie session = loginAsAdmin();

        mockMvc.perform(put("/api/admin/menus/00000000-0000-0000-0000-000000000207")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"menuName":"","screenId":"SCR-MENU-INFO","url":"/api/admin/menus","icon":"menu-link","businessDivision":"시스템 관리","description":"설명","isUsed":true,"displayOrder":2,"menuLevel":"SUB"}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fields", hasKey("menuName")));
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
