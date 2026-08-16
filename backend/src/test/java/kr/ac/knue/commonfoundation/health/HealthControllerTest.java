package kr.ac.knue.commonfoundation.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HealthController.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void openapiContractFixtureIsAvailableOnClasspath() {
        ClassPathResource openApi = new ClassPathResource("contracts/openapi.yaml");

        assertThat(openApi.exists()).isTrue();
    }

    @Test
    void getHealthReturnsApiResponseEnvelope() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.error").doesNotExist())
            .andExpect(jsonPath("$.data.status").value("UP"))
            .andExpect(jsonPath("$.data.service").value("common-foundation"))
            .andExpect(jsonPath("$.data.timestamp", notNullValue()));
    }
}
