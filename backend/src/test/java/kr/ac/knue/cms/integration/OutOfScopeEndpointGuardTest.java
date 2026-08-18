package kr.ac.knue.cms.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class OutOfScopeEndpointGuardTest {
    private static final List<String> OUT_OF_SCOPE_API_PATHS = List.of(
        "/api/admin/professor-achievements",
        "/api/admin/professor-achievement-evaluations",
        "/api/admin/academic-grants",
        "/api/admin/research-fund-applications",
        "/api/admin/files",
        "/api/admin/attachments",
        "/api/admin/excel/imports",
        "/api/admin/excel/exports",
        "/api/admin/personal-information",
        "/api/admin/access-logs",
        "/api/admin/audit-logs",
        "/api/admin/batches"
    );

    @Autowired MockMvc mockMvc;

    @Test
    void out_of_scope_business_platform_and_operations_apis_are_not_generated() throws Exception {
        Cookie adminSession = loginAsAdmin();

        for (String path : OUT_OF_SCOPE_API_PATHS) {
            mockMvc.perform(get(path).cookie(adminSession))
                .andExpect(status().isNotFound());
        }
    }

    @Test
    void out_of_scope_mutation_endpoints_are_not_generated_even_for_seed_admin() throws Exception {
        Cookie adminSession = loginAsAdmin();

        for (String path : OUT_OF_SCOPE_API_PATHS) {
            mockMvc.perform(post(path)
                    .cookie(adminSession)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isNotFound());
        }
    }

    @Test
    void out_of_scope_guard_covers_each_excluded_requirement_family() {
        assertThat(OUT_OF_SCOPE_API_PATHS)
            .anyMatch(path -> path.contains("professor-achievement"))
            .anyMatch(path -> path.contains("academic-grants") || path.contains("research-fund"))
            .anyMatch(path -> path.contains("files") || path.contains("attachments"))
            .anyMatch(path -> path.contains("excel"))
            .anyMatch(path -> path.contains("personal-information"))
            .anyMatch(path -> path.contains("access-logs"))
            .anyMatch(path -> path.contains("audit-logs"))
            .anyMatch(path -> path.contains("batches"));
    }

    private Cookie loginAsAdmin() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"admin","password":"admin"}
                    """))
            .andExpect(status().isOk())
            .andReturn();
        MockHttpServletResponse response = result.getResponse();
        return response.getCookie("SESSION");
    }
}
