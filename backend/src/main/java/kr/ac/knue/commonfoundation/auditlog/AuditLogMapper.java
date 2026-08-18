package kr.ac.knue.commonfoundation.auditlog;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;

@Mapper
public interface AuditLogMapper {

    @SelectProvider(type = AuditLogMapperSqlProvider.class, method = "selectAuditLogs")
    List<AuditLogListItem> selectAuditLogs(AuditLogSearchCondition condition);

    @SelectProvider(type = AuditLogMapperSqlProvider.class, method = "countAuditLogs")
    long countAuditLogs(AuditLogSearchCondition condition);

    @Select("select exists(select 1 from audit_logs where audit_log_id = #{auditLogId})")
    boolean existsAuditLog(@Param("auditLogId") long auditLogId);

    @Select("""
        select
            al.audit_log_id as "auditLogId",
            al.log_type as "logType",
            case al.log_type when 'LOGIN' then '로그인' when 'LOGOUT' then '로그아웃' when 'CREATE' then '등록' when 'UPDATE' then '수정' when 'DELETE' then '삭제' when 'READ' then '조회' when 'AUTHORIZATION' then '권한' else al.log_type end as "logTypeName",
            al.target_key as "targetKey",
            al.actor_id as "actorId",
            coalesce(al.before_value::text, '') as "beforeValue",
            coalesce(al.after_value::text, '') as "afterValue",
            al.result as "result",
            case al.result when 'SUCCESS' then '성공' when 'DENIED' then '거부' when 'FAILED' then '실패' else al.result end as "resultName",
            '감사로그 원문은 수정·삭제할 수 없으며 상세에서 변경 전후값을 조회합니다.' as "operationRule"
        from audit_logs al
        where al.audit_log_id = #{auditLogId}
        """)
    AuditLogListItem selectAuditLog(@Param("auditLogId") long auditLogId);

    @Insert("""
        insert into audit_logs (log_type, target_key, actor_id, before_value, after_value, result)
        values ('READ', #{targetKey}, #{actorId}, #{beforeValue}::jsonb, #{afterValue}::jsonb, 'SUCCESS')
        """)
    @Options(useGeneratedKeys = true, keyProperty = "generated.auditLogId", keyColumn = "audit_log_id")
    void insertAuditManagementLog(
        @Param("generated") GeneratedAuditLogId generated,
        @Param("targetKey") String targetKey,
        @Param("actorId") String actorId,
        @Param("beforeValue") String beforeValue,
        @Param("afterValue") String afterValue
    );
}
