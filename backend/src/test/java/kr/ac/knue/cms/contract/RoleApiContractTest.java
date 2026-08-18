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
class RoleApiContractTest {
    @Autowired MockMvc mockMvc;

    @Test
    void openapi_fixture_declares_role_management_operations() throws Exception {
        String yaml = new ClassPathResource("contracts/openapi.yaml").getContentAsString(StandardCharsets.UTF_8);
        org.assertj.core.api.Assertions.assertThat(yaml)
            .contains("/api/admin/roles", "/api/admin/roles/{roleCode}", "RoleUpdateRequest");
    }

    @Test
    void list_roles_returns_seeded_r01_to_r09_with_role_purpose() throws Exception {
        Cookie session = loginAsAdmin();

        mockMvc.perform(get("/api/admin/roles").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.meta").exists())
            .andExpect(jsonPath("$.data[*].roleCode", hasItem("R01")))
            .andExpect(jsonPath("$.data[*].roleCode", hasItem("R09")))
            .andExpect(jsonPath("$.data[0].rolePurpose").exists());
    }

    @Test
    void update_role_persists_descriptive_fields_and_keeps_role_code_unchanged() throws Exception {
        Cookie session = loginAsAdmin();

        mockMvc.perform(put("/api/admin/roles/R08")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "roleName":"점수산출 감사자",
                      "rolePurpose":"계약 테스트 목적 검토",
                      "assignmentCriteria":"감사 담당자 지정 기준",
                      "defaultDataScope":"감사 대상 범위",
                      "isUsed":true
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.roleCode").value("R08"))
            .andExpect(jsonPath("$.data.rolePurpose").value("계약 테스트 목적 검토"));

        mockMvc.perform(get("/api/admin/roles").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[?(@.roleCode=='R08')].rolePurpose", hasItem("계약 테스트 목적 검토")));
    }

    @Test
    void update_role_rejects_payload_role_code_mismatch_without_mutating_role_code() throws Exception {
        Cookie session = loginAsAdmin();

        mockMvc.perform(put("/api/admin/roles/R07")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "roleCode":"R09",
                      "roleName":"악성 변경",
                      "rolePurpose":"roleCode 변경 시도",
                      "assignmentCriteria":"허용되지 않음",
                      "defaultDataScope":"전체",
                      "isUsed":true
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.fields", hasKey("roleCode")));

        mockMvc.perform(get("/api/admin/roles").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[?(@.roleCode=='R07')].roleCode", hasItem("R07")));
    }

    @Test
    void update_role_rejects_blank_required_fields_as_api_error_fields() throws Exception {
        Cookie session = loginAsAdmin();

        mockMvc.perform(put("/api/admin/roles/R06")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"roleName":"","rolePurpose":"","assignmentCriteria":"","defaultDataScope":"","isUsed":true}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.fields", hasKey("roleName")))
            .andExpect(jsonPath("$.error.fields", hasKey("rolePurpose")))
            .andExpect(jsonPath("$.error.fields", hasKey("assignmentCriteria")))
            .andExpect(jsonPath("$.error.fields", hasKey("defaultDataScope")));
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
