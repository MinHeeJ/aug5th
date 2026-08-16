package kr.ac.knue.commonfoundation.notice;

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
public class NoticeManagementController {

    private final NoticeManagementService noticeManagementService;

    public NoticeManagementController(NoticeManagementService noticeManagementService) {
        this.noticeManagementService = noticeManagementService;
    }

    @GetMapping("/api/admin/notices")
    public ApiResponse<NoticeListResponse> listNotices(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String filter
    ) {
        return ApiResponse.ok(noticeManagementService.listNotices(NoticeSearchCondition.of(page, size, sort, q, filter)));
    }

    @PostMapping("/api/admin/notices")
    public ApiResponse<SaveNoticeResponse> saveNotice(@Valid @RequestBody SaveNoticeRequest request) {
        return ApiResponse.ok(noticeManagementService.saveNotice(request));
    }
}
