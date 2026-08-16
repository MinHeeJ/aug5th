package kr.ac.knue.commonfoundation.batchexecution;

import jakarta.validation.Valid;
import kr.ac.knue.commonfoundation.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class BatchExecutionManagementController {

    private final BatchExecutionManagementService batchExecutionManagementService;

    public BatchExecutionManagementController(BatchExecutionManagementService batchExecutionManagementService) {
        this.batchExecutionManagementService = batchExecutionManagementService;
    }

    @GetMapping("/api/admin/batch-executions")
    public ApiResponse<BatchExecutionListResponse> listBatchExecutions(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String filter
    ) {
        return ApiResponse.ok(batchExecutionManagementService.listBatchExecutions(BatchExecutionSearchCondition.of(page, size, sort, q, filter)));
    }

    @PostMapping("/api/admin/batch-executions")
    public ApiResponse<BatchExecutionActionResponse> runBatch(@Valid @RequestBody RunBatchRequest request) {
        return ApiResponse.ok(batchExecutionManagementService.runBatch(request));
    }

    @PostMapping("/api/admin/batch-executions/{executionId}/stop")
    public ApiResponse<BatchExecutionActionResponse> stopBatch(
        @PathVariable long executionId,
        @Valid @RequestBody BatchExecutionActionRequest request
    ) {
        return ApiResponse.ok(batchExecutionManagementService.stopBatch(executionId, request));
    }

    @PostMapping("/api/admin/batch-executions/{executionId}/rerun")
    public ApiResponse<BatchExecutionActionResponse> rerunBatch(
        @PathVariable long executionId,
        @Valid @RequestBody BatchExecutionActionRequest request
    ) {
        return ApiResponse.ok(batchExecutionManagementService.rerunBatch(executionId, request));
    }
}
