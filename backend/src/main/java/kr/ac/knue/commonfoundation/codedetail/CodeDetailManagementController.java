package kr.ac.knue.commonfoundation.codedetail;

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
public class CodeDetailManagementController {

    private final CodeDetailManagementService codeDetailManagementService;

    public CodeDetailManagementController(CodeDetailManagementService codeDetailManagementService) {
        this.codeDetailManagementService = codeDetailManagementService;
    }

    @GetMapping("/api/admin/code-details")
    public ApiResponse<CodeDetailListResponse> listCodeDetails(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String filter
    ) {
        return ApiResponse.ok(codeDetailManagementService.listCodeDetails(CodeDetailSearchCondition.of(page, size, sort, q, filter)));
    }

    @PostMapping("/api/admin/code-details")
    public ApiResponse<SaveCodeDetailResponse> saveCodeDetail(@Valid @RequestBody SaveCodeDetailRequest request) {
        return ApiResponse.ok(codeDetailManagementService.saveCodeDetail(request));
    }
}
