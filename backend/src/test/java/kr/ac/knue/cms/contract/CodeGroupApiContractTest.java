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
class CodeGroupApiContractTest {
    @Autowired MockMvc mockMvc;

    @Test
    void openapi_fixture_declares_code_group_operations() throws Exception {
        String yaml = new ClassPathResource("contracts/openapi.yaml").getContentAsString(StandardCharsets.UTF_8);
        org.assertj.core.api.Assertions.assertThat(yaml)
            .contains("/api/admin/code-groups", "/api/admin/code-groups/{groupId}", "CodeGroup");
    }

    @Test
    void list_code_groups_returns_seeded_common_group_with_detail_navigation_identity() throws Exception {
        Cookie session = loginAsAdmin();

        mockMvc.perform(get("/api/admin/code-groups").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.meta").exists())
            .andExpect(jsonPath("$.data[*].groupId", hasItem("COMMON")))
            .andExpect(jsonPath("$.data[?(@.groupId=='COMMON')].managingDepartment", hasItem("시스템관리부서")));
    }

    @Test
    void save_code_group_persists_group_name_description_and_department() throws Exception {
        Cookie session = loginAsAdmin();

        mockMvc.perform(put("/api/admin/code-groups/EVAL_AREA")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "groupId":"EVAL_AREA",
                      "groupName":"평가영역",
                      "description":"교수업적 평가영역 선택값",
                      "managingDepartment":"교수지원과",
                      "isUsed":true
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.groupId").value("EVAL_AREA"))
            .andExpect(jsonPath("$.data.groupName").value("평가영역"))
            .andExpect(jsonPath("$.data.managingDepartment").value("교수지원과"));

        mockMvc.perform(get("/api/admin/code-groups?filter=평가영역").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[*].groupId", hasItem("EVAL_AREA")));
    }

    @Test
    void save_code_group_rejects_path_payload_group_id_mismatch() throws Exception {
        Cookie session = loginAsAdmin();

        mockMvc.perform(put("/api/admin/code-groups/STATUS")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "groupId":"AUTH_TYPE",
                      "groupName":"인증구분",
                      "description":"잘못된 식별자",
                      "managingDepartment":"정보화팀",
                      "isUsed":true
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.fields", hasKey("groupId")));
    }

    @Test
    void save_code_group_rejects_blank_required_fields_as_api_error_fields() throws Exception {
        Cookie session = loginAsAdmin();

        mockMvc.perform(put("/api/admin/code-groups/BLANK_GROUP")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"groupId":"","groupName":"","description":"","managingDepartment":"","isUsed":true}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.fields", hasKey("groupId")))
            .andExpect(jsonPath("$.error.fields", hasKey("groupName")));
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
