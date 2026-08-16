package kr.ac.knue.commonfoundation.attachment;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AttachmentMapper {

    @SelectProvider(type = AttachmentMapperSqlProvider.class, method = "selectAttachments")
    List<AttachmentListItem> selectAttachments(AttachmentSearchCondition condition);

    @SelectProvider(type = AttachmentMapperSqlProvider.class, method = "countAttachments")
    long countAttachments(AttachmentSearchCondition condition);

    @Select("select count(*) from attachment_files")
    long countAllAttachments();

    @Select("""
        select count(*)
        from attachment_files
        where deleted = true
           or storage_present = false
           or integrity_status <> 'OK'
           or malware_scan_result in ('INFECTED', 'FAILED')
        """)
    long countAbnormalAttachments();

    @Select("""
        select exists(select 1 from attachment_files where attachment_id = #{attachmentId})
        """)
    boolean existsAttachment(@Param("attachmentId") Long attachmentId);

    @Select("""
        select af.attachment_id as "attachmentId",
               af.business_key as "businessKey",
               af.original_name as "originalName",
               af.stored_name as "storedName",
               af.extension as "extension",
               af.size_bytes as "sizeBytes",
               af.uploaded_by as "uploadedBy",
               af.uploaded_at as "uploadedAt",
               af.malware_scan_result as "malwareScanResult",
               af.deleted as "deleted",
               af.finalized_record as "finalizedRecord",
               af.storage_present as "storagePresent",
               af.integrity_status as "integrityStatus",
               '다운로드 시 권한을 재검증합니다.' as "downloadAuthorizationRule",
               '개발·검증 환경에서는 논리삭제만 허용합니다.' as "deleteBoundary"
        from attachment_files af
        where af.attachment_id = #{attachmentId}
        """)
    AttachmentListItem selectAttachment(@Param("attachmentId") Long attachmentId);

    @Update("""
        update attachment_files
        set deleted = #{deleted},
            delete_reason = #{deleteReason},
            deleted_by = #{deletedBy},
            deleted_at = case when #{deleted} then now() else null end
        where attachment_id = #{attachmentId}
        """)
    int updateLogicalDelete(
        @Param("attachmentId") Long attachmentId,
        @Param("deleted") Boolean deleted,
        @Param("deleteReason") String deleteReason,
        @Param("deletedBy") String deletedBy
    );

    @Insert("""
        insert into audit_logs (log_type, target_key, actor_id, before_value, after_value, result)
        values (#{logType}, #{targetKey}, #{actorId}, #{beforeValue}::jsonb, #{afterValue}::jsonb, 'SUCCESS')
        """)
    void insertAudit(
        @Param("logType") String logType,
        @Param("targetKey") String targetKey,
        @Param("actorId") String actorId,
        @Param("beforeValue") String beforeValue,
        @Param("afterValue") String afterValue
    );
}
