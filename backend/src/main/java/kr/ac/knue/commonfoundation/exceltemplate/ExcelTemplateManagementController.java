package kr.ac.knue.commonfoundation.exceltemplate;

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
public class ExcelTemplateManagementController {

    private final ExcelTemplateManagementService excelTemplateManagementService;

    public ExcelTemplateManagementController(ExcelTemplateManagementService excelTemplateManagementService) {
        this.excelTemplateManagementService = excelTemplateManagementService;
    }

    @GetMapping("/api/admin/excel-templates")
    public ApiResponse<ExcelTemplateListResponse> listExcelTemplates(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String filter
    ) {
        return ApiResponse.ok(excelTemplateManagementService.listExcelTemplates(ExcelTemplateSearchCondition.of(page, size, sort, q, filter)));
    }

    @PostMapping("/api/admin/excel-templates")
    public ApiResponse<SaveExcelTemplateResponse> saveExcelTemplate(@Valid @RequestBody SaveExcelTemplateRequest request) {
        return ApiResponse.ok(excelTemplateManagementService.saveExcelTemplate(request));
    }

    @GetMapping("/api/admin/excel-templates/{templateId}/download")
    public ApiResponse<ExcelTemplateDownloadResponse> downloadExcelTemplate(@PathVariable long templateId) {
        return ApiResponse.ok(excelTemplateManagementService.downloadExcelTemplate(templateId));
    }
}
