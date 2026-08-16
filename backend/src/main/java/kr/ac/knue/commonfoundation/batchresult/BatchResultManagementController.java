package kr.ac.knue.commonfoundation.batchresult;

import kr.ac.knue.commonfoundation.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class BatchResultManagementController {

    private final BatchResultManagementService batchResultManagementService;

    public BatchResultManagementController(BatchResultManagementService batchResultManagementService) {
        this.batchResultManagementService = batchResultManagementService;
    }

    @GetMapping("/api/admin/batch-results")
    public ApiResponse<BatchResultListResponse> listBatchResults(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String filter
    ) {
        return ApiResponse.ok(batchResultManagementService.listBatchResults(BatchResultSearchCondition.of(page, size, sort, q, filter)));
    }

    @GetMapping("/api/admin/batch-results/{batchResultId}/log")
    public ApiResponse<BatchResultLogResponse> getBatchResultLog(@PathVariable long batchResultId) {
        return ApiResponse.ok(batchResultManagementService.getBatchResultLog(batchResultId));
    }

    @PostMapping("/api/admin/batch-results")
    public ApiResponse<SaveBatchResultResponse> saveBatchResults() {
        return ApiResponse.ok(batchResultManagementService.recordReadOnlySaveRequest());
    }
}
