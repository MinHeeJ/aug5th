package kr.ac.knue.commonfoundation.userrole;

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
public class UserRoleManagementController {

    private final UserRoleManagementService userRoleManagementService;

    public UserRoleManagementController(UserRoleManagementService userRoleManagementService) {
        this.userRoleManagementService = userRoleManagementService;
    }

    @GetMapping("/api/admin/user-roles")
    public ApiResponse<UserRoleListResponse> listUserRoles(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String filter
    ) {
        return ApiResponse.ok(userRoleManagementService.listUserRoles(UserRoleSearchCondition.of(page, size, sort, q, filter)));
    }

    @PostMapping("/api/admin/user-roles")
    public ApiResponse<SaveUserRoleResponse> saveUserRole(@Valid @RequestBody SaveUserRoleRequest request) {
        return ApiResponse.ok(userRoleManagementService.saveUserRole(request));
    }
}
