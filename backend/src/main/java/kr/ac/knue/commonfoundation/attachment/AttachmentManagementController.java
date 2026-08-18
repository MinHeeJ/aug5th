package kr.ac.knue.commonfoundation.attachment;

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
public class AttachmentManagementController {

    private final AttachmentManagementService attachmentManagementService;

    public AttachmentManagementController(AttachmentManagementService attachmentManagementService) {
        this.attachmentManagementService = attachmentManagementService;
    }

    @GetMapping("/api/admin/attachments")
    public ApiResponse<AttachmentListResponse> listAttachments(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String filter
    ) {
        return ApiResponse.ok(attachmentManagementService.listAttachments(AttachmentSearchCondition.of(page, size, sort, q, filter)));
    }

    @PostMapping("/api/admin/attachments")
    public ApiResponse<SaveAttachmentResponse> saveAttachment(@Valid @RequestBody SaveAttachmentRequest request) {
        return ApiResponse.ok(attachmentManagementService.saveAttachment(request));
    }

    @PostMapping("/api/admin/attachments/{attachmentId}/delete")
    public ApiResponse<SaveAttachmentResponse> deleteAttachment(
        @PathVariable long attachmentId,
        @Valid @RequestBody(required = false) DeleteAttachmentRequest request
    ) {
        return ApiResponse.ok(attachmentManagementService.deleteAttachment(attachmentId, request));
    }

    @PostMapping("/api/admin/attachments/integrity-checks")
    public ApiResponse<AttachmentIntegrityCheckResponse> runAttachmentIntegrityCheck() {
        return ApiResponse.ok(attachmentManagementService.runIntegrityCheck());
    }
}
