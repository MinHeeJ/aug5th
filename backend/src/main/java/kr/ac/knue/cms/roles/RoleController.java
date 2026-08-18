package kr.ac.knue.cms.roles;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import kr.ac.knue.cms.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/api/admin/roles")
    public ApiResponse<List<Map<String, Object>>> listRoles() {
        return ApiResponse.ok(roleService.listRoles());
    }

    @PutMapping("/api/admin/roles/{roleCode}")
    public ApiResponse<Map<String, Object>> updateRole(@PathVariable String roleCode, @Valid @RequestBody RoleUpdateRequest request) {
        return ApiResponse.ok(roleService.updateRole(roleCode, request));
    }
}
