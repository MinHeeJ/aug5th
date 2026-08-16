package kr.ac.knue.commonfoundation.exceldownload;

import org.apache.ibatis.jdbc.SQL;

public final class ExcelDownloadMapperSqlProvider {

    private ExcelDownloadMapperSqlProvider() {
    }

    public static String selectExcelDownloads(ExcelDownloadSearchCondition condition) {
        return baseSelect(condition)
            + " order by " + orderBy(condition.sort())
            + " limit #{size} offset #{offset}";
    }

    public static String countExcelDownloads(ExcelDownloadSearchCondition condition) {
        return "select count(*) from (" + baseSelect(condition) + ") counted_excel_downloads";
    }

    private static String baseSelect(ExcelDownloadSearchCondition condition) {
        SQL sql = new SQL()
            .SELECT("edr.download_id as \"downloadId\"")
            .SELECT("edr.requester_id as \"requesterId\"")
            .SELECT("edr.query_condition::text as \"queryCondition\"")
            .SELECT("edr.data_scope_applied::text as \"dataScopeApplied\"")
            .SELECT("edr.file_id as \"fileId\"")
            .SELECT("af.original_name as \"fileName\"")
            .SELECT("af.extension as \"extension\"")
            .SELECT("af.size_bytes as \"sizeBytes\"")
            .SELECT("edr.created_at as \"createdAt\"")
            .SELECT("'현재 조회조건과 사용자 데이터범위 권한을 적용하여 생성합니다.' as \"generationRule\"")
            .SELECT("'원천 업무자료는 변경하지 않고 권한 밖 자료는 포함하지 않습니다.' as \"boundaryRule\"")
            .FROM("excel_download_requests edr")
            .LEFT_OUTER_JOIN("attachment_files af on af.attachment_id = edr.file_id");
        if (condition.q() != null) {
            sql.WHERE("(edr.requester_id ilike '%' || #{q} || '%' or edr.query_condition::text ilike '%' || #{q} || '%' or coalesce(af.original_name, '') ilike '%' || #{q} || '%')");
        }
        if (condition.requesterId() != null) {
            sql.WHERE("edr.requester_id = #{requesterId}");
        }
        if (condition.fileId() != null) {
            sql.WHERE("edr.file_id = #{fileId}");
        }
        return sql.toString();
    }

    private static String orderBy(String sort) {
        return switch (sort == null ? "" : sort) {
            case "requesterId" -> "edr.requester_id asc, edr.download_id desc";
            case "fileId" -> "edr.file_id asc nulls last, edr.download_id desc";
            default -> "edr.created_at desc, edr.download_id desc";
        };
    }
}
