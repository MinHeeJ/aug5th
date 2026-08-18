package kr.ac.knue.commonfoundation.organization;

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
public class OrganizationManagementController {

    private final OrganizationManagementService organizationManagementService;

    public OrganizationManagementController(OrganizationManagementService organizationManagementService) {
        this.organizationManagementService = organizationManagementService;
    }

    @GetMapping("/api/admin/organizations")
    public ApiResponse<OrganizationListResponse> listOrganizations(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String filter
    ) {
        return ApiResponse.ok(organizationManagementService.listOrganizations(OrganizationSearchCondition.of(page, size, sort, q, filter)));
    }

    @PostMapping("/api/admin/organizations")
    public ApiResponse<SaveOrganizationResponse> saveOrganization(@Valid @RequestBody SaveOrganizationRequest request) {
        return ApiResponse.ok(organizationManagementService.saveOrganization(request));
    }
}
