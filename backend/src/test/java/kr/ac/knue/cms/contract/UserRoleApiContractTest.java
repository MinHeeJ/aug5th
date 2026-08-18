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
class UserRoleApiContractTest {
    private static final String FACULTY_USER_ID = "20000000-0000-0000-0000-000000000002";
    private static final String ADMIN_USER_ID = "20000000-0000-0000-0000-000000000001";

    @Autowired MockMvc mockMvc;

    @Test
    void openapi_fixture_declares_user_role_management_operations() throws Exception {
        String yaml = new ClassPathResource("contracts/openapi.yaml").getContentAsString(StandardCharsets.UTF_8);
        org.assertj.core.api.Assertions.assertThat(yaml)
            .contains("/api/admin/user-roles", "/api/admin/user-roles/{userId}", "UserRolesUpdateRequest");
    }

    @Test
    void list_user_roles_returns_current_roles_valid_period_and_assignment_type() throws Exception {
        Cookie session = loginAsAdmin();

        mockMvc.perform(get("/api/admin/user-roles?userId=" + FACULTY_USER_ID).cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.meta").exists())
            .andExpect(jsonPath("$.data[*].userId", hasItem(FACULTY_USER_ID)))
            .andExpect(jsonPath("$.data[*].roleCode", hasItem("R01")))
            .andExpect(jsonPath("$.data[*].assignmentType", hasItem("POSITION")))
            .andExpect(jsonPath("$.data[0].validFrom").exists())
            .andExpect(jsonPath("$.data[0].approvedByUserId").exists());
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = {
        "delete from user_roles where user_id = '20000000-0000-0000-0000-000000000002'",
        "insert into user_roles (user_id, role_code, assignment_type, valid_from, approved_by_user_id, after_value, change_reason, is_used) values ('20000000-0000-0000-0000-000000000002', 'R01', 'POSITION', current_date, '20000000-0000-0000-0000-000000000001', 'R01', '예시 교원 사용자', true)"
    })
    void save_user_roles_grants_manual_role_and_preserves_approver_and_valid_period() throws Exception {
        Cookie session = loginAsAdmin();

        mockMvc.perform(put("/api/admin/user-roles/" + FACULTY_USER_ID)
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "roles":[{
                        "roleCode":"R03",
                        "assignmentType":"MANUAL",
                        "validFrom":"2026-01-01",
                        "validTo":"2026-12-31",
                        "approvedByUserId":"20000000-0000-0000-0000-000000000001",
                        "isUsed":true
                      }]
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[*].roleCode", hasItem("R03")))
            .andExpect(jsonPath("$.data[?(@.roleCode=='R03')].assignmentType", hasItem("MANUAL")))
            .andExpect(jsonPath("$.data[?(@.roleCode=='R03')].validTo", hasItem("2026-12-31")))
            .andExpect(jsonPath("$.data[?(@.roleCode=='R03')].approvedByUserId", hasItem(ADMIN_USER_ID)));
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = {
        "delete from user_roles where user_id = '20000000-0000-0000-0000-000000000002'",
        "insert into user_roles (user_id, role_code, assignment_type, valid_from, approved_by_user_id, after_value, change_reason, is_used) values ('20000000-0000-0000-0000-000000000002', 'R01', 'POSITION', current_date, '20000000-0000-0000-0000-000000000001', 'R01', '예시 교원 사용자', true)"
    })
    void save_user_roles_revokes_role_by_marking_inactive_instead_of_deleting() throws Exception {
        Cookie session = loginAsAdmin();

        mockMvc.perform(put("/api/admin/user-roles/" + FACULTY_USER_ID)
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "roles":[{
                        "roleCode":"R03",
                        "assignmentType":"MANUAL",
                        "validFrom":"2026-01-01",
                        "approvedByUserId":"20000000-0000-0000-0000-000000000001",
                        "isUsed":false
                      }]
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[*].roleCode", hasItem("R03")))
            .andExpect(jsonPath("$.data[?(@.roleCode=='R03')].isUsed", hasItem(false)))
            .andExpect(jsonPath("$.data[?(@.roleCode=='R03')].revokedAt").exists());
    }

    @Test
    void save_user_roles_rejects_empty_roles_array() throws Exception {
        Cookie session = loginAsAdmin();

        mockMvc.perform(put("/api/admin/user-roles/" + FACULTY_USER_ID)
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roles\":[]}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.fields", hasKey("roles")));
    }

    @Test
    void save_user_roles_rejects_invalid_valid_period_without_mutating() throws Exception {
        Cookie session = loginAsAdmin();

        mockMvc.perform(put("/api/admin/user-roles/" + FACULTY_USER_ID)
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "roles":[{
                        "roleCode":"R04",
                        "assignmentType":"MANUAL",
                        "validFrom":"2026-12-31",
                        "validTo":"2026-01-01",
                        "approvedByUserId":"20000000-0000-0000-0000-000000000001",
                        "isUsed":true
                      }]
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.fields", hasKey("validTo")));
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
