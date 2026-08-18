package kr.ac.knue.commonfoundation.filepolicy;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface FilePolicyMapper {

    @SelectProvider(type = FilePolicyMapperSqlProvider.class, method = "selectFilePolicies")
    List<FilePolicyListItem> selectFilePolicies(FilePolicySearchCondition condition);

    @SelectProvider(type = FilePolicyMapperSqlProvider.class, method = "countFilePolicies")
    long countFilePolicies(FilePolicySearchCondition condition);

    @Select("""
        select exists(select 1 from file_policies where file_policy_id = #{filePolicyId})
        """)
    boolean existsFilePolicy(@Param("filePolicyId") Long filePolicyId);

    @Select("""
        select fp.file_policy_id as "filePolicyId",
               fp.business_area as "businessArea",
               coalesce(cd.code_name, fp.business_area) as "businessAreaName",
               fp.allowed_extensions as "allowedExtensions",
               fp.max_file_size_mb as "maxFileSizeMb",
               fp.max_file_count as "maxFileCount",
               fp.max_total_size_mb as "maxTotalSizeMb",
               fp.max_filename_length as "maxFilenameLength",
               fp.malware_scan_enabled as "malwareScanEnabled",
               fp.enabled as "enabled",
               '첨부파일 업로드 검증 시 확장자·용량·개수·파일명 길이 정책을 적용합니다.' as "uploadValidationRule",
               '이 화면에서는 실제 파일 업로드·조회·삭제를 수행하지 않습니다.' as "fileOperationBoundary"
        from file_policies fp
        left join code_details cd on cd.group_id = 'BUSINESS_AREA' and cd.code_value = fp.business_area
        where fp.file_policy_id = #{filePolicyId}
        """)
    FilePolicyListItem selectFilePolicy(@Param("filePolicyId") Long filePolicyId);

    @Update("""
        update file_policies
        set allowed_extensions = #{allowedExtensions},
            max_file_size_mb = #{maxFileSizeMb},
            max_file_count = #{maxFileCount},
            max_total_size_mb = #{maxTotalSizeMb},
            max_filename_length = #{maxFilenameLength},
            malware_scan_enabled = #{malwareScanEnabled},
            enabled = #{enabled}
        where file_policy_id = #{filePolicyId}
        """)
    int updateFilePolicy(
        @Param("filePolicyId") Long filePolicyId,
        @Param("allowedExtensions") String allowedExtensions,
        @Param("maxFileSizeMb") Integer maxFileSizeMb,
        @Param("maxFileCount") Integer maxFileCount,
        @Param("maxTotalSizeMb") Integer maxTotalSizeMb,
        @Param("maxFilenameLength") Integer maxFilenameLength,
        @Param("malwareScanEnabled") Boolean malwareScanEnabled,
        @Param("enabled") Boolean enabled
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
