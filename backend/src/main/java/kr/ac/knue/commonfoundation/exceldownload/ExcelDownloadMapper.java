package kr.ac.knue.commonfoundation.exceldownload;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

@Mapper
public interface ExcelDownloadMapper {

    @SelectProvider(type = ExcelDownloadMapperSqlProvider.class, method = "selectExcelDownloads")
    List<ExcelDownloadListItem> selectExcelDownloads(ExcelDownloadSearchCondition condition);

    @SelectProvider(type = ExcelDownloadMapperSqlProvider.class, method = "countExcelDownloads")
    long countExcelDownloads(ExcelDownloadSearchCondition condition);

    @Insert("""
        insert into attachment_files (
            business_key, original_name, stored_name, extension, size_bytes,
            uploaded_by, uploaded_at, malware_scan_result, deleted, finalized_record,
            storage_present, integrity_status
        ) values (
            #{businessKey}, #{fileName}, #{storedName}, 'xlsx', #{sizeBytes},
            #{requesterId}, now(), 'CLEAN', false, false,
            true, 'OK'
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "holder.downloadId", keyColumn = "attachment_id")
    int insertGeneratedFile(
        @Param("holder") GeneratedDownloadId holder,
        @Param("businessKey") String businessKey,
        @Param("fileName") String fileName,
        @Param("storedName") String storedName,
        @Param("sizeBytes") long sizeBytes,
        @Param("requesterId") String requesterId
    );

    @Insert("""
        insert into excel_download_requests (requester_id, query_condition, data_scope_applied, file_id, created_at)
        values (#{requesterId}, #{queryCondition}::jsonb, #{dataScopeApplied}::jsonb, #{fileId}, now())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "holder.downloadId", keyColumn = "download_id")
    int insertDownloadRequest(
        @Param("holder") GeneratedDownloadId holder,
        @Param("requesterId") String requesterId,
        @Param("queryCondition") String queryCondition,
        @Param("dataScopeApplied") String dataScopeApplied,
        @Param("fileId") Long fileId
    );

    @Insert("""
        insert into audit_logs (log_type, target_key, actor_id, before_value, after_value, result)
        values (#{logType}, #{targetKey}, #{actorId}, #{beforeValue}::jsonb, #{afterValue}::jsonb, #{result})
        """)
    void insertAudit(
        @Param("logType") String logType,
        @Param("targetKey") String targetKey,
        @Param("actorId") String actorId,
        @Param("beforeValue") String beforeValue,
        @Param("afterValue") String afterValue,
        @Param("result") String result
    );
}
