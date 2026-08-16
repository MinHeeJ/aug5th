package kr.ac.knue.commonfoundation.menu;

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
public class MenuManagementController {

    private final MenuManagementService menuManagementService;

    public MenuManagementController(MenuManagementService menuManagementService) {
        this.menuManagementService = menuManagementService;
    }

    @GetMapping("/api/admin/menus")
    public ApiResponse<MenuListResponse> listMenus(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String filter
    ) {
        return ApiResponse.ok(menuManagementService.listMenus(MenuSearchCondition.of(page, size, sort, q, filter)));
    }

    @PostMapping("/api/admin/menus")
    public ApiResponse<SaveMenuResponse> saveMenu(@Valid @RequestBody SaveMenuRequest request) {
        return ApiResponse.ok(menuManagementService.saveMenu(request));
    }
}
