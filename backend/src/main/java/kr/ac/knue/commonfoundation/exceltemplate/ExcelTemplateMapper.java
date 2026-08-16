package kr.ac.knue.commonfoundation.exceltemplate;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ExcelTemplateMapper {

    @SelectProvider(type = ExcelTemplateMapperSqlProvider.class, method = "selectExcelTemplates")
    List<ExcelTemplateListItem> selectExcelTemplates(ExcelTemplateSearchCondition condition);

    @SelectProvider(type = ExcelTemplateMapperSqlProvider.class, method = "countExcelTemplates")
    long countExcelTemplates(ExcelTemplateSearchCondition condition);

    @Select("""
        select exists(select 1 from excel_templates where template_id = #{templateId})
        """)
    boolean existsExcelTemplate(@Param("templateId") Long templateId);

    @Select("""
        select et.template_id as "templateId",
               et.business_area as "businessArea",
               coalesce(cd.code_name, et.business_area) as "businessAreaName",
               et.version as "version",
               et.required_columns::text as "requiredColumns",
               jsonb_array_length(et.required_columns) as "requiredColumnCount",
               et.effective_date as "effectiveDate",
               et.download_file_id as "downloadFileId",
               af.original_name as "downloadFileName",
               et.enabled as "enabled",
               '필수값·타입·중복규칙을 템플릿 버전으로 검증합니다.' as "validationRule",
               '다운로드 시 첨부파일 권한과 템플릿 사용여부를 재검증합니다.' as "downloadRule",
               et.updated_at as "updatedAt"
        from excel_templates et
        left join code_details cd on cd.group_id = 'BUSINESS_AREA' and cd.code_value = et.business_area
        left join attachment_files af on af.attachment_id = et.download_file_id
        where et.template_id = #{templateId}
        """)
    ExcelTemplateListItem selectExcelTemplate(@Param("templateId") Long templateId);

    @Select("""
        select exists(
            select 1
            from excel_templates
            where business_area = #{businessArea}
              and version = #{version}
              and template_id <> #{templateId}
        )
        """)
    boolean existsDuplicateVersion(
        @Param("templateId") Long templateId,
        @Param("businessArea") String businessArea,
        @Param("version") String version
    );

    @Update("""
        update excel_templates
        set required_columns = #{requiredColumnsJson}::jsonb,
            effective_date = #{effectiveDate},
            enabled = #{enabled},
            updated_by = #{updatedBy},
            updated_at = now()
        where template_id = #{templateId}
        """)
    int updateExcelTemplate(
        @Param("templateId") Long templateId,
        @Param("requiredColumnsJson") String requiredColumnsJson,
        @Param("effectiveDate") LocalDate effectiveDate,
        @Param("enabled") Boolean enabled,
        @Param("updatedBy") String updatedBy
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
