package kr.ac.knue.commonfoundation.baseyear;

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
public class BaseYearManagementController {

    private final BaseYearManagementService baseYearManagementService;

    public BaseYearManagementController(BaseYearManagementService baseYearManagementService) {
        this.baseYearManagementService = baseYearManagementService;
    }

    @GetMapping("/api/admin/base-years")
    public ApiResponse<BaseYearListResponse> getBaseYears(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String filter
    ) {
        return ApiResponse.ok(baseYearManagementService.getBaseYears(BaseYearSearchCondition.of(page, size, sort, q, filter)));
    }

    @PostMapping("/api/admin/base-years")
    public ApiResponse<SaveBaseYearResponse> saveBaseYears(@Valid @RequestBody SaveBaseYearRequest request) {
        return ApiResponse.ok(baseYearManagementService.saveBaseYear(request));
    }
}
