package kr.ac.knue.cms.menus;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kr.ac.knue.cms.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MenuInformationController {
    private final MenuAdminService service;

    public MenuInformationController(MenuAdminService service) {
        this.service = service;
    }

    @GetMapping("/api/admin/menus")
    public ApiResponse<List<Map<String, Object>>> listMenus() {
        return ApiResponse.ok(service.listMenus());
    }

    @PutMapping("/api/admin/menus/{menuId}")
    public ApiResponse<Map<String, Object>> saveMenu(@PathVariable UUID menuId, @Valid @RequestBody MenuRequest request) {
        return ApiResponse.ok(service.saveInformation(menuId, request));
    }

    @PatchMapping("/api/admin/menus/{menuId}/status")
    public ApiResponse<Map<String, Object>> updateMenuStatus(@PathVariable UUID menuId, @Valid @RequestBody MenuStatusRequest request) {
        return ApiResponse.ok(service.updateStatus(menuId, request));
    }
}
