package kr.ac.knue.commonfoundation.foundation;

import java.util.Map;

import kr.ac.knue.commonfoundation.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class FoundationAccessController {

    private final FoundationAccessService foundationAccessService;

    public FoundationAccessController(FoundationAccessService foundationAccessService) {
        this.foundationAccessService = foundationAccessService;
    }

    @GetMapping("/api/admin/foundation/data-scope")
    public ApiResponse<Map<String, Object>> dataScope() {
        return ApiResponse.ok(foundationAccessService.currentDataScope());
    }
}
