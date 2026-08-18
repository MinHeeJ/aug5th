package kr.ac.knue.cms.contract;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

@SpringBootTest
@AutoConfigureMockMvc
class OrganizationApiContractTest {
    @Autowired MockMvc mockMvc;

    @Test
    void openapi_contract_fixture_contains_organization_operations() throws Exception {
        String yaml = new ClassPathResource("contracts/openapi.yaml").getContentAsString(StandardCharsets.UTF_8);
        org.assertj.core.api.Assertions.assertThat(yaml)
            .contains("/api/admin/organizations", "/api/admin/organization-tree", "saveOrganizationRelationship");
    }

    @Test
    void list_organizations_returns_code_name_type_and_usage() throws Exception {
        mockMvc.perform(get("/api/admin/organizations").cookie(adminSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[*].organizationCode", hasItem("KNUE")))
            .andExpect(jsonPath("$.data[*].organizationName", hasItem("한국교원대학교")))
            .andExpect(jsonPath("$.data[*].organizationType", hasItem("UNIVERSITY")))
            .andExpect(jsonPath("$.data[*].isUsed").exists());
    }

    @Test
    void organization_tree_returns_parent_child_relationships() throws Exception {
        mockMvc.perform(get("/api/admin/organization-tree").cookie(adminSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[*].organizationCode", hasItem("KNUE")))
            .andExpect(jsonPath("$.data[*].children[*].organizationCode", hasItem("EDU-COL")));
    }

    @Test
    void save_relationship_persists_effective_period_and_change_reason() throws Exception {
        mockMvc.perform(put("/api/admin/organization-relationships/{relationshipId}", "30000000-0000-0000-0000-000000000001")
                .cookie(adminSession())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"organizationId":"10000000-0000-0000-0000-000000000002","parentOrganizationId":"10000000-0000-0000-0000-000000000001","effectiveStartDate":"2026-01-01","effectiveEndDate":"2026-12-31","changeReason":"contract test"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.relationshipId").value("30000000-0000-0000-0000-000000000001"))
            .andExpect(jsonPath("$.data.effectiveStartDate").value("2026-01-01"))
            .andExpect(jsonPath("$.data.effectiveEndDate").value("2026-12-31"))
            .andExpect(jsonPath("$.data.changeReason").value("contract test"));
    }

    @Test
    void save_relationship_rejects_reversed_effective_period() throws Exception {
        mockMvc.perform(put("/api/admin/organization-relationships/{relationshipId}", "30000000-0000-0000-0000-000000000001")
                .cookie(adminSession())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"organizationId":"10000000-0000-0000-0000-000000000002","parentOrganizationId":"10000000-0000-0000-0000-000000000001","effectiveStartDate":"2026-12-31","effectiveEndDate":"2026-01-01"}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.fields", hasKey("effectiveEndDate")));
    }

    @Test
    void organization_apis_require_r09_session() throws Exception {
        mockMvc.perform(get("/api/admin/organizations"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
    }

    private jakarta.servlet.http.Cookie adminSession() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"admin\"}"))
            .andExpect(status().isOk())
            .andReturn();
        return result.getResponse().getCookie("SESSION");
    }
}
