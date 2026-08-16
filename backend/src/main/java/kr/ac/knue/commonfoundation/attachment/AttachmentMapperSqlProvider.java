package kr.ac.knue.commonfoundation.attachment;

import org.apache.ibatis.jdbc.SQL;

public final class AttachmentMapperSqlProvider {

    private AttachmentMapperSqlProvider() {
    }

    public static String selectAttachments(AttachmentSearchCondition condition) {
        return baseSelect(condition)
            + " order by " + orderBy(condition.sort())
            + " limit #{size} offset #{offset}";
    }

    public static String countAttachments(AttachmentSearchCondition condition) {
        return "select count(*) from (" + baseSelect(condition) + ") counted_attachments";
    }

    private static String baseSelect(AttachmentSearchCondition condition) {
        SQL sql = new SQL()
            .SELECT("af.attachment_id as \"attachmentId\"")
            .SELECT("af.business_key as \"businessKey\"")
            .SELECT("af.original_name as \"originalName\"")
            .SELECT("af.stored_name as \"storedName\"")
            .SELECT("af.extension as \"extension\"")
            .SELECT("af.size_bytes as \"sizeBytes\"")
            .SELECT("af.uploaded_by as \"uploadedBy\"")
            .SELECT("af.uploaded_at as \"uploadedAt\"")
            .SELECT("af.malware_scan_result as \"malwareScanResult\"")
            .SELECT("af.deleted as \"deleted\"")
            .SELECT("af.finalized_record as \"finalizedRecord\"")
            .SELECT("af.storage_present as \"storagePresent\"")
            .SELECT("af.integrity_status as \"integrityStatus\"")
            .SELECT("'다운로드 시 권한을 재검증합니다.' as \"downloadAuthorizationRule\"")
            .SELECT("'개발·검증 환경에서는 논리삭제만 허용합니다.' as \"deleteBoundary\"")
            .FROM("attachment_files af");
        if (condition.q() != null) {
            sql.WHERE("(af.business_key ilike '%' || #{q} || '%' or af.original_name ilike '%' || #{q} || '%' or af.stored_name ilike '%' || #{q} || '%' or af.uploaded_by ilike '%' || #{q} || '%')");
        }
        if (condition.businessKey() != null) {
            sql.WHERE("af.business_key = #{businessKey}");
        }
        if (condition.malwareScanResult() != null) {
            sql.WHERE("af.malware_scan_result = #{malwareScanResult}");
        }
        if (condition.deleted() != null) {
            sql.WHERE("af.deleted = #{deleted}");
        }
        if (condition.integrityStatus() != null) {
            sql.WHERE("af.integrity_status = #{integrityStatus}");
        }
        return sql.toString();
    }

    private static String orderBy(String sort) {
        return switch (sort == null ? "" : sort) {
            case "businessKey" -> "af.business_key asc, af.attachment_id desc";
            case "originalName" -> "af.original_name asc, af.attachment_id desc";
            case "sizeBytes" -> "af.size_bytes desc, af.attachment_id desc";
            case "malwareScanResult" -> "af.malware_scan_result asc, af.attachment_id desc";
            default -> "af.uploaded_at desc, af.attachment_id desc";
        };
    }
}
