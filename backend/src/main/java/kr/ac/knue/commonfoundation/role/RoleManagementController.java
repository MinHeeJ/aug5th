package kr.ac.knue.commonfoundation.role;

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
public class RoleManagementController {

    private final RoleManagementService roleManagementService;

    public RoleManagementController(RoleManagementService roleManagementService) {
        this.roleManagementService = roleManagementService;
    }

    @GetMapping("/api/admin/roles")
    public ApiResponse<RoleListResponse> listRoles(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String filter
    ) {
        return ApiResponse.ok(roleManagementService.listRoles(RoleSearchCondition.of(page, size, sort, q, filter)));
    }

    @PostMapping("/api/admin/roles")
    public ApiResponse<SaveRoleResponse> saveRole(@Valid @RequestBody SaveRoleRequest request) {
        return ApiResponse.ok(roleManagementService.saveRole(request));
    }
}
