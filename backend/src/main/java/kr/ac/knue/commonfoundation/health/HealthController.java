package kr.ac.knue.commonfoundation.health;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Map;

import kr.ac.knue.commonfoundation.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class HealthController {

    private final Clock clock;

    public HealthController() {
        this(Clock.systemUTC());
    }

    HealthController(Clock clock) {
        this.clock = clock;
    }

    @GetMapping("/api/health")
    public ApiResponse<Map<String, String>> getHealth() {
        return ApiResponse.ok(Map.of(
            "status", "UP",
            "service", "common-foundation",
            "timestamp", OffsetDateTime.now(clock).toString()
        ));
    }
}
