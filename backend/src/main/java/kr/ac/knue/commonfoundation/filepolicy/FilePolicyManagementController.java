package kr.ac.knue.commonfoundation.filepolicy;

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
public class FilePolicyManagementController {

    private final FilePolicyManagementService filePolicyManagementService;

    public FilePolicyManagementController(FilePolicyManagementService filePolicyManagementService) {
        this.filePolicyManagementService = filePolicyManagementService;
    }

    @GetMapping("/api/admin/file-policies")
    public ApiResponse<FilePolicyListResponse> listFilePolicies(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String filter
    ) {
        return ApiResponse.ok(filePolicyManagementService.listFilePolicies(FilePolicySearchCondition.of(page, size, sort, q, filter)));
    }

    @PostMapping("/api/admin/file-policies")
    public ApiResponse<SaveFilePolicyResponse> saveFilePolicy(@Valid @RequestBody SaveFilePolicyRequest request) {
        return ApiResponse.ok(filePolicyManagementService.saveFilePolicy(request));
    }
}
