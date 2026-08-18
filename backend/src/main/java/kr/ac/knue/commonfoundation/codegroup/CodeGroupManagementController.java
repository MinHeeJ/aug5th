package kr.ac.knue.commonfoundation.codegroup;

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
public class CodeGroupManagementController {

    private final CodeGroupManagementService codeGroupManagementService;

    public CodeGroupManagementController(CodeGroupManagementService codeGroupManagementService) {
        this.codeGroupManagementService = codeGroupManagementService;
    }

    @GetMapping("/api/admin/code-groups")
    public ApiResponse<CodeGroupListResponse> listCodeGroups(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String filter
    ) {
        return ApiResponse.ok(codeGroupManagementService.listCodeGroups(CodeGroupSearchCondition.of(page, size, sort, q, filter)));
    }

    @PostMapping("/api/admin/code-groups")
    public ApiResponse<SaveCodeGroupResponse> saveCodeGroup(@Valid @RequestBody SaveCodeGroupRequest request) {
        return ApiResponse.ok(codeGroupManagementService.saveCodeGroup(request));
    }
}
