package kr.ac.knue.cms.menus;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kr.ac.knue.cms.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MenuStructureController {
    private final MenuAdminService service;

    public MenuStructureController(MenuAdminService service) {
        this.service = service;
    }

    @GetMapping("/api/admin/menu-structure")
    public ApiResponse<List<Map<String, Object>>> getMenuStructure() {
        return ApiResponse.ok(service.listMenus());
    }

    @PutMapping("/api/admin/menu-structure/{menuId}")
    public ApiResponse<Map<String, Object>> saveMenuStructure(@PathVariable UUID menuId, @Valid @RequestBody MenuRequest request) {
        return ApiResponse.ok(service.saveStructure(menuId, request));
    }

    @PutMapping("/api/admin/menu-structure/reorder")
    public ApiResponse<List<Map<String, Object>>> reorderMenuStructure(@Valid @RequestBody MenuReorderRequest request) {
        return ApiResponse.ok(service.reorder(request));
    }
}
