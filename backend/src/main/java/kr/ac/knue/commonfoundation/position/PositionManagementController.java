package kr.ac.knue.commonfoundation.position;

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
public class PositionManagementController {

    private final PositionManagementService positionManagementService;

    public PositionManagementController(PositionManagementService positionManagementService) {
        this.positionManagementService = positionManagementService;
    }

    @GetMapping("/api/admin/positions")
    public ApiResponse<PositionListResponse> listPositions(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String filter
    ) {
        return ApiResponse.ok(positionManagementService.listPositions(PositionSearchCondition.of(page, size, sort, q, filter)));
    }

    @PostMapping("/api/admin/positions")
    public ApiResponse<SavePositionResponse> savePosition(@Valid @RequestBody SavePositionRequest request) {
        return ApiResponse.ok(positionManagementService.savePosition(request));
    }
}
