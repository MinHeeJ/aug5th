package kr.ac.knue.commonfoundation.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(ApiEnvelopeContractTest.ProbeController.class)
@Import(GlobalApiExceptionHandler.class)
class ApiEnvelopeContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void successfulResponsesUseApiResponseEnvelope() throws Exception {
        mockMvc.perform(get("/api/probe/success"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.value").value("정상"))
            .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void apiExceptionsUseStableApiErrorEnvelopeWithoutSensitiveDetails() throws Exception {
        mockMvc.perform(get("/api/probe/forbidden"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.data").doesNotExist())
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"))
            .andExpect(jsonPath("$.error.message").value("권한이 없습니다."));
    }

    @Test
    void validationErrorsReturnFieldMapInApiError() throws Exception {
        mockMvc.perform(post("/api/probe/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.fields.name").exists());
    }

    @RestController
    @RequestMapping("/api/probe")
    static class ProbeController {

        @GetMapping("/success")
        ApiResponse<ProbeResponse> success() {
            return ApiResponse.ok(new ProbeResponse("정상"));
        }

        @GetMapping("/forbidden")
        ApiResponse<Void> forbidden() {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "권한이 없습니다.");
        }

        @PostMapping("/validate")
        ApiResponse<ProbeResponse> validate(@Valid @RequestBody ProbeRequest request) {
            return ApiResponse.ok(new ProbeResponse(request.name()));
        }
    }

    record ProbeResponse(String value) {
    }

    record ProbeRequest(@NotBlank(message = "이름을 입력하세요.") String name) {
    }
}
