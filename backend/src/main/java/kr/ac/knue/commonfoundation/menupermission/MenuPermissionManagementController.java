package kr.ac.knue.commonfoundation.menupermission;

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
public class MenuPermissionManagementController {

    private final MenuPermissionManagementService menuPermissionManagementService;

    public MenuPermissionManagementController(MenuPermissionManagementService menuPermissionManagementService) {
        this.menuPermissionManagementService = menuPermissionManagementService;
    }

    @GetMapping("/api/admin/menu-permissions")
    public ApiResponse<MenuPermissionListResponse> listMenuPermissions(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String filter
    ) {
        return ApiResponse.ok(menuPermissionManagementService.listMenuPermissions(MenuPermissionSearchCondition.of(page, size, sort, q, filter)));
    }

    @PostMapping("/api/admin/menu-permissions")
    public ApiResponse<SaveMenuPermissionResponse> saveMenuPermission(@Valid @RequestBody SaveMenuPermissionRequest request) {
        return ApiResponse.ok(menuPermissionManagementService.saveMenuPermission(request));
    }
}
