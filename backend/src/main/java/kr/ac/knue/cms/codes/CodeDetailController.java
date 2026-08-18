package kr.ac.knue.cms.codes;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import kr.ac.knue.cms.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CodeDetailController {
    private final CodeService codeService;

    public CodeDetailController(CodeService codeService) {
        this.codeService = codeService;
    }

    @GetMapping("/api/admin/code-groups/{groupId}/codes")
    public ApiResponse<List<Map<String, Object>>> listCodes(@PathVariable String groupId) {
        return ApiResponse.ok(codeService.listCodes(groupId));
    }

    @PutMapping("/api/admin/code-groups/{groupId}/codes/{codeValue}")
    public ApiResponse<Map<String, Object>> saveCode(@PathVariable String groupId, @PathVariable String codeValue, @Valid @RequestBody Code request) {
        return ApiResponse.ok(codeService.saveCode(groupId, codeValue, request));
    }
}
