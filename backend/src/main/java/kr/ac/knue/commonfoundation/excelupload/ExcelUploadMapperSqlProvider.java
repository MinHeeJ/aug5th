package kr.ac.knue.commonfoundation.excelupload;

import org.apache.ibatis.jdbc.SQL;

public final class ExcelUploadMapperSqlProvider {

    private ExcelUploadMapperSqlProvider() {
    }

    public static String selectExcelUploads(ExcelUploadSearchCondition condition) {
        return baseSelect(condition)
            + " order by " + orderBy(condition.sort())
            + " limit #{size} offset #{offset}";
    }

    public static String countExcelUploads(ExcelUploadSearchCondition condition) {
        return "select count(*) from (" + baseSelect(condition) + ") counted_excel_uploads";
    }

    private static String baseSelect(ExcelUploadSearchCondition condition) {
        SQL sql = new SQL()
            .SELECT("euh.upload_id as \"uploadId\"")
            .SELECT("euh.template_id as \"templateId\"")
            .SELECT("et.business_area as \"businessArea\"")
            .SELECT("coalesce(cd.code_name, et.business_area) as \"businessAreaName\"")
            .SELECT("et.version as \"version\"")
            .SELECT("euh.uploader_id as \"uploaderId\"")
            .SELECT("euh.file_name as \"fileName\"")
            .SELECT("euh.total_count as \"totalCount\"")
            .SELECT("euh.success_count as \"successCount\"")
            .SELECT("euh.error_count as \"errorCount\"")
            .SELECT("euh.excluded_count as \"excludedCount\"")
            .SELECT("euh.saved_count as \"savedCount\"")
            .SELECT("euh.processing_time_ms as \"processingTimeMs\"")
            .SELECT("euh.upload_status as \"uploadStatus\"")
            .SELECT("euh.uploaded_at as \"uploadedAt\"")
            .SELECT("'모든 행이 정상일 때만 하나의 트랜잭션으로 등록합니다.' as \"transactionRule\"")
            .SELECT("'업무별 확정 양식 버전과 헤더·필수값·형식·코드·중복을 검증합니다.' as \"validationRule\"")
            .FROM("excel_upload_histories euh")
            .JOIN("excel_templates et on et.template_id = euh.template_id")
            .LEFT_OUTER_JOIN("code_details cd on cd.group_id = 'BUSINESS_AREA' and cd.code_value = et.business_area");
        if (condition.q() != null) {
            sql.WHERE("(euh.file_name ilike '%' || #{q} || '%' or euh.uploader_id ilike '%' || #{q} || '%' or et.business_area ilike '%' || #{q} || '%' or et.version ilike '%' || #{q} || '%')");
        }
        if (condition.templateId() != null) {
            sql.WHERE("euh.template_id = #{templateId}");
        }
        if (condition.uploadStatus() != null) {
            sql.WHERE("euh.upload_status = #{uploadStatus}");
        }
        return sql.toString();
    }

    private static String orderBy(String sort) {
        return switch (sort == null ? "" : sort) {
            case "fileName" -> "euh.file_name asc, euh.upload_id desc";
            case "templateId" -> "euh.template_id asc, euh.upload_id desc";
            case "processingTime" -> "euh.processing_time_ms desc, euh.upload_id desc";
            default -> "euh.uploaded_at desc, euh.upload_id desc";
        };
    }
}
