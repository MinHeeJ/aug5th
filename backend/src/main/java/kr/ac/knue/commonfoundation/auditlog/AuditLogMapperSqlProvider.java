package kr.ac.knue.commonfoundation.auditlog;

import org.apache.ibatis.jdbc.SQL;

public final class AuditLogMapperSqlProvider {

    private AuditLogMapperSqlProvider() {
    }

    public static String selectAuditLogs(AuditLogSearchCondition condition) {
        return baseSelect(condition)
            + " order by " + orderBy(condition.sort())
            + " limit #{size} offset #{offset}";
    }

    public static String countAuditLogs(AuditLogSearchCondition condition) {
        return "select count(*) from (" + baseSelect(condition) + ") counted_audit_logs";
    }

    private static String baseSelect(AuditLogSearchCondition condition) {
        SQL sql = new SQL()
            .SELECT("al.audit_log_id as \"auditLogId\"")
            .SELECT("al.log_type as \"logType\"")
            .SELECT("case al.log_type when 'LOGIN' then '로그인' when 'LOGOUT' then '로그아웃' when 'CREATE' then '등록' when 'UPDATE' then '수정' when 'DELETE' then '삭제' when 'READ' then '조회' when 'AUTHORIZATION' then '권한' else al.log_type end as \"logTypeName\"")
            .SELECT("al.target_key as \"targetKey\"")
            .SELECT("al.actor_id as \"actorId\"")
            .SELECT("coalesce(al.before_value::text, '') as \"beforeValue\"")
            .SELECT("coalesce(al.after_value::text, '') as \"afterValue\"")
            .SELECT("al.result as \"result\"")
            .SELECT("case al.result when 'SUCCESS' then '성공' when 'DENIED' then '거부' when 'FAILED' then '실패' else al.result end as \"resultName\"")
            .SELECT("'감사로그 원문은 수정·삭제할 수 없으며 상세에서 변경 전후값을 조회합니다.' as \"operationRule\"")
            .FROM("audit_logs al");
        if (condition.q() != null) {
            sql.WHERE("(al.target_key ilike '%' || #{q} || '%' or al.actor_id ilike '%' || #{q} || '%' or al.log_type ilike '%' || #{q} || '%' or al.result ilike '%' || #{q} || '%')");
        }
        if (condition.logType() != null) {
            sql.WHERE("al.log_type = #{logType}");
        }
        if (condition.result() != null) {
            sql.WHERE("al.result = #{result}");
        }
        if (condition.actorId() != null) {
            sql.WHERE("al.actor_id = #{actorId}");
        }
        return sql.toString();
    }

    private static String orderBy(String sort) {
        return switch (sort == null ? "" : sort) {
            case "logType" -> "al.log_type asc, al.audit_log_id desc";
            case "actorId" -> "al.actor_id asc, al.audit_log_id desc";
            case "targetKey" -> "al.target_key asc, al.audit_log_id desc";
            case "result" -> "al.result asc, al.audit_log_id desc";
            default -> "al.audit_log_id desc";
        };
    }
}
