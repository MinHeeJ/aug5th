package kr.ac.knue.cms.contract;

import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

@SpringBootTest
@AutoConfigureMockMvc
class ApiEnvelopeContractTest {
    @Autowired MockMvc mockMvc;

    @Test
    void openapi_contract_fixture_is_loaded_from_classpath() throws Exception {
        ClassPathResource resource = new ClassPathResource("contracts/openapi.yaml");
        String yaml = resource.getContentAsString(StandardCharsets.UTF_8);
        org.assertj.core.api.Assertions.assertThat(yaml).contains("/api/auth/login", "ApiResponse", "ApiError");
    }

    @Test
    void every_success_response_contains_api_response_envelope() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.meta").exists())
            .andExpect(jsonPath("$.data.status").value("UP"));
    }

    @Test
    void every_error_response_contains_api_error_envelope_and_meta() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.meta").exists())
            .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"))
            .andExpect(jsonPath("$.error.message").exists());
    }

    @Test
    void validation_errors_are_returned_as_field_error_envelope() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.meta").exists())
            .andExpect(jsonPath("$.error.fields", hasKey("username")))
            .andExpect(jsonPath("$.error.fields", hasKey("password")));
    }

    @Test
    void successful_login_sets_http_only_same_site_lax_session_cookie() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"admin","password":"admin"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(cookie().httpOnly("SESSION", true));
    }
}
