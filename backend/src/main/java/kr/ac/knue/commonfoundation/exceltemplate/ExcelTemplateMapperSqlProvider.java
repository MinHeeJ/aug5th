package kr.ac.knue.commonfoundation.exceltemplate;

import org.apache.ibatis.jdbc.SQL;

public final class ExcelTemplateMapperSqlProvider {

    private ExcelTemplateMapperSqlProvider() {
    }

    public static String selectExcelTemplates(ExcelTemplateSearchCondition condition) {
        return baseSelect(condition)
            + " order by " + orderBy(condition.sort())
            + " limit #{size} offset #{offset}";
    }

    public static String countExcelTemplates(ExcelTemplateSearchCondition condition) {
        return "select count(*) from (" + baseSelect(condition) + ") counted_excel_templates";
    }

    private static String baseSelect(ExcelTemplateSearchCondition condition) {
        SQL sql = new SQL()
            .SELECT("et.template_id as \"templateId\"")
            .SELECT("et.business_area as \"businessArea\"")
            .SELECT("coalesce(cd.code_name, et.business_area) as \"businessAreaName\"")
            .SELECT("et.version as \"version\"")
            .SELECT("et.required_columns::text as \"requiredColumns\"")
            .SELECT("jsonb_array_length(et.required_columns) as \"requiredColumnCount\"")
            .SELECT("et.effective_date as \"effectiveDate\"")
            .SELECT("et.download_file_id as \"downloadFileId\"")
            .SELECT("af.original_name as \"downloadFileName\"")
            .SELECT("et.enabled as \"enabled\"")
            .SELECT("'필수값·타입·중복규칙을 템플릿 버전으로 검증합니다.' as \"validationRule\"")
            .SELECT("'다운로드 시 첨부파일 권한과 템플릿 사용여부를 재검증합니다.' as \"downloadRule\"")
            .SELECT("et.updated_at as \"updatedAt\"")
            .FROM("excel_templates et")
            .LEFT_OUTER_JOIN("code_details cd on cd.group_id = 'BUSINESS_AREA' and cd.code_value = et.business_area")
            .LEFT_OUTER_JOIN("attachment_files af on af.attachment_id = et.download_file_id");
        if (condition.q() != null) {
            sql.WHERE("(et.business_area ilike '%' || #{q} || '%' or et.version ilike '%' || #{q} || '%' or coalesce(cd.code_name, '') ilike '%' || #{q} || '%')");
        }
        if (condition.businessArea() != null) {
            sql.WHERE("et.business_area = #{businessArea}");
        }
        if (condition.enabled() != null) {
            sql.WHERE("et.enabled = #{enabled}");
        }
        return sql.toString();
    }

    private static String orderBy(String sort) {
        return switch (sort == null ? "" : sort) {
            case "businessArea" -> "et.business_area asc, et.version desc, et.template_id desc";
            case "version" -> "et.version desc, et.template_id desc";
            case "effectiveDate" -> "et.effective_date desc, et.template_id desc";
            default -> "et.updated_at desc, et.template_id desc";
        };
    }
}
