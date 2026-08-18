package kr.ac.knue.commonfoundation.batchdefinition;

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
public class BatchDefinitionManagementController {

    private final BatchDefinitionManagementService batchDefinitionManagementService;

    public BatchDefinitionManagementController(BatchDefinitionManagementService batchDefinitionManagementService) {
        this.batchDefinitionManagementService = batchDefinitionManagementService;
    }

    @GetMapping("/api/admin/batch-definitions")
    public ApiResponse<BatchDefinitionListResponse> listBatchDefinitions(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String filter
    ) {
        return ApiResponse.ok(batchDefinitionManagementService.listBatchDefinitions(BatchDefinitionSearchCondition.of(page, size, sort, q, filter)));
    }

    @PostMapping("/api/admin/batch-definitions")
    public ApiResponse<SaveBatchDefinitionResponse> saveBatchDefinition(@Valid @RequestBody SaveBatchDefinitionRequest request) {
        return ApiResponse.ok(batchDefinitionManagementService.saveBatchDefinition(request));
    }
}
