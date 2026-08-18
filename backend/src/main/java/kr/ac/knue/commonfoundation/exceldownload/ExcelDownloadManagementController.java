package kr.ac.knue.commonfoundation.exceldownload;

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
public class ExcelDownloadManagementController {

    private final ExcelDownloadManagementService excelDownloadManagementService;

    public ExcelDownloadManagementController(ExcelDownloadManagementService excelDownloadManagementService) {
        this.excelDownloadManagementService = excelDownloadManagementService;
    }

    @GetMapping("/api/admin/excel-downloads")
    public ApiResponse<ExcelDownloadListResponse> listExcelDownloads(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String filter
    ) {
        return ApiResponse.ok(excelDownloadManagementService.listExcelDownloads(ExcelDownloadSearchCondition.of(page, size, sort, q, filter)));
    }

    @PostMapping("/api/admin/excel-downloads")
    public ApiResponse<CreateExcelDownloadResponse> createExcelDownload(@Valid @RequestBody CreateExcelDownloadRequest request) {
        return ApiResponse.ok(excelDownloadManagementService.createExcelDownload(request));
    }
}
