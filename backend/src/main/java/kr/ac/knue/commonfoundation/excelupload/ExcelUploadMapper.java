package kr.ac.knue.commonfoundation.excelupload;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;

@Mapper
public interface ExcelUploadMapper {

    @SelectProvider(type = ExcelUploadMapperSqlProvider.class, method = "selectExcelUploads")
    List<ExcelUploadListItem> selectExcelUploads(ExcelUploadSearchCondition condition);

    @SelectProvider(type = ExcelUploadMapperSqlProvider.class, method = "countExcelUploads")
    long countExcelUploads(ExcelUploadSearchCondition condition);

    @Select("""
        select euh.upload_id as "uploadId",
               euh.template_id as "templateId",
               et.business_area as "businessArea",
               coalesce(cd.code_name, et.business_area) as "businessAreaName",
               et.version as "version",
               euh.uploader_id as "uploaderId",
               euh.file_name as "fileName",
               euh.total_count as "totalCount",
               euh.success_count as "successCount",
               euh.error_count as "errorCount",
               coalesce(euh.excluded_count, 0) as "excludedCount",
               coalesce(euh.saved_count, euh.success_count) as "savedCount",
               coalesce(euh.processing_time_ms, 0) as "processingTimeMs",
               euh.upload_status as "uploadStatus",
               euh.uploaded_at as "uploadedAt",
               '오류 발생 시 전체 행을 반영하지 않고 오류목록을 제공합니다.' as "transactionRule",
               '템플릿의 필수값·타입·중복규칙을 적용합니다.' as "validationRule"
        from excel_upload_histories euh
        join excel_templates et on et.template_id = euh.template_id
        left join code_details cd on cd.group_id = 'BUSINESS_AREA' and cd.code_value = et.business_area
        where euh.upload_id = #{uploadId}
        """)
    ExcelUploadListItem selectExcelUpload(@Param("uploadId") Long uploadId);

    @Select("select count(*) from excel_upload_errors where upload_id = #{uploadId}")
    long countUploadErrors(@Param("uploadId") Long uploadId);

    @Select("""
        select et.template_id as "templateId",
               et.business_area as "businessArea",
               et.version as "version",
               et.required_columns::text as "requiredColumns",
               et.enabled as "enabled"
        from excel_templates et
        where et.template_id = #{templateId}
        """)
    ExcelUploadTemplate selectTemplate(@Param("templateId") Long templateId);

    @Insert("""
        insert into excel_upload_histories (
            template_id, uploader_id, file_name, total_count, success_count,
            error_count, excluded_count, saved_count, processing_time_ms,
            upload_status, uploaded_at
        ) values (
            #{templateId}, #{uploaderId}, #{fileName}, #{totalCount}, #{successCount},
            #{errorCount}, #{excludedCount}, #{savedCount}, #{processingTimeMs},
            #{uploadStatus}, now()
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "holder.uploadId", keyColumn = "upload_id")
    int insertUploadHistory(
        @Param("holder") GeneratedUploadId holder,
        @Param("templateId") Long templateId,
        @Param("uploaderId") String uploaderId,
        @Param("fileName") String fileName,
        @Param("totalCount") int totalCount,
        @Param("successCount") int successCount,
        @Param("errorCount") int errorCount,
        @Param("excludedCount") int excludedCount,
        @Param("savedCount") int savedCount,
        @Param("processingTimeMs") int processingTimeMs,
        @Param("uploadStatus") String uploadStatus
    );

    @Insert("""
        insert into excel_upload_errors (upload_id, row_number, column_name, input_value, error_code, error_reason)
        values (#{uploadId}, #{error.rowNumber}, #{error.columnName}, #{error.inputValue}, #{error.errorCode}, #{error.errorReason})
        """)
    void insertUploadError(@Param("uploadId") Long uploadId, @Param("error") ExcelUploadValidationError error);

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
