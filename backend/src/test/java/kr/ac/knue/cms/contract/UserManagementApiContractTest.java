package kr.ac.knue.cms.contract;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

@SpringBootTest
@AutoConfigureMockMvc
class UserManagementApiContractTest {
    @Autowired MockMvc mockMvc;

    @Test
    void openapi_contract_fixture_contains_user_management_operations() throws Exception {
        String yaml = new ClassPathResource("contracts/openapi.yaml").getContentAsString(StandardCharsets.UTF_8);
        org.assertj.core.api.Assertions.assertThat(yaml)
            .contains("/api/admin/users", "updateUserSystemAccess", "UserSystemAccessRequest");
    }

    @Test
    void list_users_returns_korus_readonly_columns_and_local_access_state() throws Exception {
        mockMvc.perform(get("/api/admin/users").cookie(adminSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].userId").exists())
            .andExpect(jsonPath("$.data[*].staffId", hasItem("STAFF-001")))
            .andExpect(jsonPath("$.data[*].positionTitle", hasItem("학과장")))
            .andExpect(jsonPath("$.data[*].retirementDate").exists())
            .andExpect(jsonPath("$.data[*].lastSyncedAt").exists())
            .andExpect(jsonPath("$.data[*].isSystemEnabled").exists())
            .andExpect(jsonPath("$.data[*].roles").isArray());
    }

    @Test
    void list_users_applies_safe_filter_without_null_bound_predicates() throws Exception {
        mockMvc.perform(get("/api/admin/users").param("filter", "홍길동").cookie(adminSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].staffName").value("홍길동"));
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = {
        "update users set is_system_enabled = true, status = 'ACTIVE', updated_at = now() where user_id = '20000000-0000-0000-0000-000000000002'",
        "delete from user_roles where user_id = '20000000-0000-0000-0000-000000000002'",
        "insert into user_roles (user_id, role_code, assignment_type, valid_from, approved_by_user_id, after_value, change_reason, is_used) values ('20000000-0000-0000-0000-000000000002', 'R01', 'POSITION', current_date, '20000000-0000-0000-0000-000000000001', 'R01', '예시 교원 사용자', true)"
    })
    void update_system_access_changes_only_local_system_enabled_and_roles() throws Exception {
        mockMvc.perform(patch("/api/admin/users/{userId}/system-access", "20000000-0000-0000-0000-000000000002")
                .cookie(adminSession())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"isSystemEnabled":false,"roleCodes":["R02","R09"],"changeReason":"contract test"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.userId").value("20000000-0000-0000-0000-000000000002"))
            .andExpect(jsonPath("$.data.isSystemEnabled").value(false))
            .andExpect(jsonPath("$.data.staffName").value("홍길동"))
            .andExpect(jsonPath("$.data.roles", containsInAnyOrder("R02", "R09")));
    }

    @Test
    void update_system_access_rejects_missing_required_local_fields() throws Exception {
        mockMvc.perform(patch("/api/admin/users/{userId}/system-access", "20000000-0000-0000-0000-000000000002")
                .cookie(adminSession())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.fields", hasKey("isSystemEnabled")))
            .andExpect(jsonPath("$.error.fields", hasKey("roleCodes")));
    }

    @Test
    void user_management_requires_r09_session() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
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
