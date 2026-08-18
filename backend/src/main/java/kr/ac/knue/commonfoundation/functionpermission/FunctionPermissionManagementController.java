package kr.ac.knue.commonfoundation.functionpermission;

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
public class FunctionPermissionManagementController {

    private final FunctionPermissionManagementService functionPermissionManagementService;

    public FunctionPermissionManagementController(FunctionPermissionManagementService functionPermissionManagementService) {
        this.functionPermissionManagementService = functionPermissionManagementService;
    }

    @GetMapping("/api/admin/function-permissions")
    public ApiResponse<FunctionPermissionListResponse> listFunctionPermissions(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String filter
    ) {
        return ApiResponse.ok(functionPermissionManagementService.listFunctionPermissions(FunctionPermissionSearchCondition.of(page, size, sort, q, filter)));
    }

    @PostMapping("/api/admin/function-permissions")
    public ApiResponse<SaveFunctionPermissionResponse> saveFunctionPermission(@Valid @RequestBody SaveFunctionPermissionRequest request) {
        return ApiResponse.ok(functionPermissionManagementService.saveFunctionPermission(request));
    }
}
