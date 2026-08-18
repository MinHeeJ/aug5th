package kr.ac.knue.cms.organizations;

import java.util.List;
import kr.ac.knue.cms.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrganizationController {
    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping("/api/admin/organizations")
    public ApiResponse<List<OrganizationDto>> listOrganizations(@RequestParam(required = false) String filter,
                                                                @RequestParam(required = false) String organizationCode,
                                                                @RequestParam(required = false) String organizationType,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(organizationService.listOrganizations(filter, organizationCode, organizationType, page, size));
    }

    @GetMapping("/api/admin/organization-tree")
    public ApiResponse<List<OrganizationTreeNode>> getOrganizationTree() {
        return ApiResponse.ok(organizationService.getTree());
    }
}
