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
class CodeDetailApiContractTest {
    @Autowired MockMvc mockMvc;

    @Test
    void openapi_fixture_declares_code_detail_operations() throws Exception {
        String yaml = new ClassPathResource("contracts/openapi.yaml").getContentAsString(StandardCharsets.UTF_8);
        org.assertj.core.api.Assertions.assertThat(yaml)
            .contains("/api/admin/code-groups/{groupId}/codes", "/api/admin/code-groups/{groupId}/codes/{codeValue}", "Code");
    }

    @Test
    void save_and_list_codes_preserves_parent_hierarchy_extra_attributes_valid_period_and_usage() throws Exception {
        Cookie session = loginAsAdmin();
        saveGroup(session, "STATUS", "처리상태");

        String parentId = mockMvc.perform(put("/api/admin/code-groups/STATUS/codes/OPEN")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "groupId":"STATUS",
                      "codeValue":"OPEN",
                      "codeName":"접수",
                      "sortOrder":1,
                      "extraAttributes":{"externalSystem":"KORUS","mappingCode":"01"},
                      "validFrom":"2026-01-01",
                      "validTo":"2026-12-31",
                      "isUsed":true
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.codeValue").value("OPEN"))
            .andExpect(jsonPath("$.data.extraAttributes.externalSystem").value("KORUS"))
            .andReturn().getResponse().getContentAsString();

        String codeId = com.jayway.jsonpath.JsonPath.read(parentId, "$.data.codeId");

        mockMvc.perform(put("/api/admin/code-groups/STATUS/codes/CLOSED")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "groupId":"STATUS",
                      "codeValue":"CLOSED",
                      "codeName":"마감",
                      "parentCodeId":"%s",
                      "sortOrder":2,
                      "extraAttributes":{"externalSystem":"KORUS","mappingCode":"99"},
                      "validFrom":"2026-01-01",
                      "isUsed":false
                    }
                    """.formatted(codeId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.parentCodeId").value(codeId))
            .andExpect(jsonPath("$.data.isUsed").value(false));

        mockMvc.perform(get("/api/admin/code-groups/STATUS/codes").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[*].codeValue", hasItem("OPEN")))
            .andExpect(jsonPath("$.data[*].codeValue", hasItem("CLOSED")))
            .andExpect(jsonPath("$.data[?(@.codeValue=='CLOSED')].parentCodeId", hasItem(codeId)))
            .andExpect(jsonPath("$.data[?(@.codeValue=='CLOSED')].extraAttributes.mappingCode", hasItem("99")));
    }

    @Test
    void save_code_rejects_blank_required_fields_and_reversed_valid_period() throws Exception {
        Cookie session = loginAsAdmin();
        saveGroup(session, "VALIDATION_STATUS", "검증상태");

        mockMvc.perform(put("/api/admin/code-groups/VALIDATION_STATUS/codes/BAD")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "groupId":"VALIDATION_STATUS",
                      "codeValue":"",
                      "codeName":"",
                      "sortOrder":1,
                      "isUsed":true
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.fields", hasKey("codeValue")))
            .andExpect(jsonPath("$.error.fields", hasKey("codeName")));

        mockMvc.perform(put("/api/admin/code-groups/VALIDATION_STATUS/codes/BAD_PERIOD")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "groupId":"VALIDATION_STATUS",
                      "codeValue":"BAD_PERIOD",
                      "codeName":"기간 오류",
                      "sortOrder":1,
                      "validFrom":"2026-12-31",
                      "validTo":"2026-01-01",
                      "isUsed":true
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.fields", hasKey("validTo")));
    }

    @Test
    void save_code_rejects_missing_group_without_creating_orphan_code() throws Exception {
        Cookie session = loginAsAdmin();

        mockMvc.perform(put("/api/admin/code-groups/NO_SUCH_GROUP/codes/ORPHAN")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "groupId":"NO_SUCH_GROUP",
                      "codeValue":"ORPHAN",
                      "codeName":"고아 코드",
                      "sortOrder":1,
                      "isUsed":true
                    }
                    """))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.fields", hasKey("groupId")));
    }

    private void saveGroup(Cookie session, String groupId, String groupName) throws Exception {
        mockMvc.perform(put("/api/admin/code-groups/" + groupId)
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"groupId":"%s","groupName":"%s","description":"테스트 그룹","managingDepartment":"시스템관리부서","isUsed":true}
                    """.formatted(groupId, groupName)))
            .andExpect(status().isOk());
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
