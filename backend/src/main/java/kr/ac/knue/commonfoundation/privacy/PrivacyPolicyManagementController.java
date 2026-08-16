package kr.ac.knue.commonfoundation.privacy;

import jakarta.validation.Valid;
import kr.ac.knue.commonfoundation.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class PrivacyPolicyManagementController {

    private final PrivacyPolicyManagementService privacyPolicyManagementService;

    public PrivacyPolicyManagementController(PrivacyPolicyManagementService privacyPolicyManagementService) {
        this.privacyPolicyManagementService = privacyPolicyManagementService;
    }

    @GetMapping("/api/admin/privacy-policies")
    public ApiResponse<PrivacyPolicyListResponse> listPrivacyPolicies(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String filter
    ) {
        return ApiResponse.ok(privacyPolicyManagementService.listPrivacyPolicies(PrivacyPolicySearchCondition.of(page, size, sort, q, filter)));
    }

    @PostMapping("/api/admin/privacy-policies")
    public ApiResponse<SavePrivacyPolicyResponse> savePrivacyPolicy(@Valid @RequestBody SavePrivacyPolicyRequest request) {
        return ApiResponse.ok(privacyPolicyManagementService.savePrivacyPolicy(request));
    }
}
