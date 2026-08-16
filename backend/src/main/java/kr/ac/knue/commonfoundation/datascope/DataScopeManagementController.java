package kr.ac.knue.commonfoundation.datascope;

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
public class DataScopeManagementController {

    private final DataScopeManagementService dataScopeManagementService;

    public DataScopeManagementController(DataScopeManagementService dataScopeManagementService) {
        this.dataScopeManagementService = dataScopeManagementService;
    }

    @GetMapping("/api/admin/data-scopes")
    public ApiResponse<DataScopeListResponse> listDataScopes(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String filter
    ) {
        return ApiResponse.ok(dataScopeManagementService.listDataScopes(DataScopeSearchCondition.of(page, size, sort, q, filter)));
    }

    @PostMapping("/api/admin/data-scopes")
    public ApiResponse<SaveDataScopeResponse> saveDataScope(@Valid @RequestBody SaveDataScopeRequest request) {
        return ApiResponse.ok(dataScopeManagementService.saveDataScope(request));
    }
}
