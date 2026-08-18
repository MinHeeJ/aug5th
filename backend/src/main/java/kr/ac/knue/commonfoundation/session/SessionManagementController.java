package kr.ac.knue.commonfoundation.session;

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
public class SessionManagementController {

    private final SessionManagementService sessionManagementService;

    public SessionManagementController(SessionManagementService sessionManagementService) {
        this.sessionManagementService = sessionManagementService;
    }

    @GetMapping("/api/admin/sessions")
    public ApiResponse<SessionListResponse> listActiveSessions(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String filter
    ) {
        return ApiResponse.ok(sessionManagementService.listActiveSessions(SessionSearchCondition.of(page, size, sort, q, filter)));
    }

    @PostMapping("/api/admin/sessions")
    public ApiResponse<SaveSessionResponse> saveActiveSession(@Valid @RequestBody SaveSessionRequest request) {
        return ApiResponse.ok(sessionManagementService.saveActiveSession(request));
    }

    @PostMapping("/api/admin/sessions/{sessionId}/terminate")
    public ApiResponse<SaveSessionResponse> terminateSession(
        @PathVariable String sessionId,
        @Valid @RequestBody TerminateSessionRequest request
    ) {
        return ApiResponse.ok(sessionManagementService.terminateSession(sessionId, request));
    }
}
