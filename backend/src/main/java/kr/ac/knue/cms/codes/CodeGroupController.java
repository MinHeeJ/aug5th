package kr.ac.knue.cms.codes;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import kr.ac.knue.cms.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CodeGroupController {
    private final CodeGroupService codeGroupService;

    public CodeGroupController(CodeGroupService codeGroupService) {
        this.codeGroupService = codeGroupService;
    }

    @GetMapping("/api/admin/code-groups")
    public ApiResponse<List<Map<String, Object>>> listCodeGroups(@RequestParam(required = false) String filter) {
        return ApiResponse.ok(codeGroupService.listCodeGroups(filter));
    }

    @PutMapping("/api/admin/code-groups/{groupId}")
    public ApiResponse<Map<String, Object>> saveCodeGroup(@PathVariable String groupId, @Valid @RequestBody CodeGroup request) {
        return ApiResponse.ok(codeGroupService.saveCodeGroup(groupId, request));
    }
}
