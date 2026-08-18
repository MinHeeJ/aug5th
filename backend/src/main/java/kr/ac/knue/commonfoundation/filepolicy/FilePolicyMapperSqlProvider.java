package kr.ac.knue.commonfoundation.filepolicy;

import org.apache.ibatis.jdbc.SQL;

public final class FilePolicyMapperSqlProvider {

    private FilePolicyMapperSqlProvider() {
    }

    public static String selectFilePolicies(FilePolicySearchCondition condition) {
        return baseSelect(condition)
            + " order by " + orderBy(condition.sort())
            + " limit #{size} offset #{offset}";
    }

    public static String countFilePolicies(FilePolicySearchCondition condition) {
        return "select count(*) from (" + baseSelect(condition) + ") counted_file_policies";
    }

    private static String baseSelect(FilePolicySearchCondition condition) {
        SQL sql = new SQL()
            .SELECT("fp.file_policy_id as \"filePolicyId\"")
            .SELECT("fp.business_area as \"businessArea\"")
            .SELECT("coalesce(cd.code_name, fp.business_area) as \"businessAreaName\"")
            .SELECT("fp.allowed_extensions as \"allowedExtensions\"")
            .SELECT("fp.max_file_size_mb as \"maxFileSizeMb\"")
            .SELECT("fp.max_file_count as \"maxFileCount\"")
            .SELECT("fp.max_total_size_mb as \"maxTotalSizeMb\"")
            .SELECT("fp.max_filename_length as \"maxFilenameLength\"")
            .SELECT("fp.malware_scan_enabled as \"malwareScanEnabled\"")
            .SELECT("fp.enabled as \"enabled\"")
            .SELECT("'첨부파일 업로드 검증 시 확장자·용량·개수·파일명 길이 정책을 적용합니다.' as \"uploadValidationRule\"")
            .SELECT("'이 화면에서는 실제 파일 업로드·조회·삭제를 수행하지 않습니다.' as \"fileOperationBoundary\"")
            .FROM("file_policies fp")
            .LEFT_OUTER_JOIN("code_details cd on cd.group_id = 'BUSINESS_AREA' and cd.code_value = fp.business_area");
        if (condition.q() != null) {
            sql.WHERE("(fp.business_area ilike '%' || #{q} || '%' or fp.allowed_extensions ilike '%' || #{q} || '%' or coalesce(cd.code_name, '') ilike '%' || #{q} || '%')");
        }
        if (condition.businessArea() != null) {
            sql.WHERE("fp.business_area = #{businessArea}");
        }
        if (condition.malwareScanEnabled() != null) {
            sql.WHERE("fp.malware_scan_enabled = #{malwareScanEnabled}");
        }
        if (condition.enabled() != null) {
            sql.WHERE("fp.enabled = #{enabled}");
        }
        return sql.toString();
    }

    private static String orderBy(String sort) {
        return switch (sort == null ? "" : sort) {
            case "businessArea" -> "fp.business_area asc, fp.file_policy_id asc";
            case "maxFileSizeMb" -> "fp.max_file_size_mb desc, fp.file_policy_id asc";
            case "maxFileCount" -> "fp.max_file_count desc, fp.file_policy_id asc";
            case "malwareScanEnabled" -> "fp.malware_scan_enabled desc, fp.file_policy_id asc";
            default -> "fp.file_policy_id asc";
        };
    }
}
