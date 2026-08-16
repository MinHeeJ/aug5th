package kr.ac.knue.commonfoundation.excelupload;

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
public class ExcelUploadManagementController {

    private final ExcelUploadManagementService excelUploadManagementService;

    public ExcelUploadManagementController(ExcelUploadManagementService excelUploadManagementService) {
        this.excelUploadManagementService = excelUploadManagementService;
    }

    @GetMapping("/api/admin/excel-uploads")
    public ApiResponse<ExcelUploadListResponse> listExcelUploads(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String filter
    ) {
        return ApiResponse.ok(excelUploadManagementService.listExcelUploads(ExcelUploadSearchCondition.of(page, size, sort, q, filter)));
    }

    @PostMapping("/api/admin/excel-uploads")
    public ApiResponse<UploadExcelResponse> uploadExcel(@Valid @RequestBody UploadExcelRequest request) {
        return ApiResponse.ok(excelUploadManagementService.uploadExcel(request));
    }

    @GetMapping("/api/admin/excel-uploads/{uploadId}/errors/download")
    public ApiResponse<ExcelUploadErrorDownloadResponse> downloadExcelUploadErrors(@PathVariable long uploadId) {
        return ApiResponse.ok(excelUploadManagementService.downloadExcelUploadErrors(uploadId));
    }
}
