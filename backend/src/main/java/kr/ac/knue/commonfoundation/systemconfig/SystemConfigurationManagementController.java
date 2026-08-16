package kr.ac.knue.commonfoundation.systemconfig;

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
public class SystemConfigurationManagementController {

    private final SystemConfigurationManagementService systemConfigurationManagementService;

    public SystemConfigurationManagementController(SystemConfigurationManagementService systemConfigurationManagementService) {
        this.systemConfigurationManagementService = systemConfigurationManagementService;
    }

    @GetMapping("/api/admin/system-configurations")
    public ApiResponse<SystemConfigurationListResponse> getSystemConfigurations(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String filter
    ) {
        return ApiResponse.ok(systemConfigurationManagementService.getSystemConfigurations(SystemConfigurationSearchCondition.of(page, size, sort, q, filter)));
    }

    @PostMapping("/api/admin/system-configurations")
    public ApiResponse<SaveSystemConfigurationResponse> saveSystemConfiguration(@Valid @RequestBody SaveSystemConfigurationRequest request) {
        return ApiResponse.ok(systemConfigurationManagementService.saveSystemConfiguration(request));
    }
}
