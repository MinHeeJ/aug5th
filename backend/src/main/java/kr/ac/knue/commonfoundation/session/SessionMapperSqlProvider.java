package kr.ac.knue.commonfoundation.session;

import org.apache.ibatis.jdbc.SQL;

public final class SessionMapperSqlProvider {

    private SessionMapperSqlProvider() {
    }

    public static String selectSessions(SessionSearchCondition condition) {
        return baseSelect(condition)
            + " order by " + orderBy(condition.sort())
            + " limit #{size} offset #{offset}";
    }

    public static String countSessions(SessionSearchCondition condition) {
        return "select count(*) from (" + baseSelect(condition) + ") counted_user_sessions";
    }

    private static String baseSelect(SessionSearchCondition condition) {
        SQL sql = new SQL()
            .SELECT("us.session_id as \"sessionId\"")
            .SELECT("us.user_id as \"userId\"")
            .SELECT("coalesce(nullif(kps.name_encrypted, ''), us.user_id) as \"userDisplayName\"")
            .SELECT("to_char(us.login_at, 'YYYY-MM-DD') || 'T' || to_char(us.login_at, 'HH24:MI:SS') as \"loginAt\"")
            .SELECT("to_char(us.last_activity_at, 'YYYY-MM-DD') || 'T' || to_char(us.last_activity_at, 'HH24:MI:SS') as \"lastActivityAt\"")
            .SELECT("us.ip_address as \"ipAddress\"")
            .SELECT("us.session_status as \"sessionStatus\"")
            .SELECT("case us.session_status when 'ACTIVE' then '활성' when 'LOGOUT' then '로그아웃' when 'IDLE_EXPIRED' then '유휴만료' when 'ABSOLUTE_EXPIRED' then '절대만료' when 'EXPIRED' then '만료' when 'TERMINATED' then '강제종료' else us.session_status end as \"sessionStatusName\"")
            .SELECT("sth.termination_id as \"latestTerminationId\"")
            .SELECT("sth.termination_type as \"latestTerminationType\"")
            .SELECT("case when us.session_status = 'ACTIVE' then '강제종료 가능: 활성 세션이며 R09 사유 입력 필요' else '종료 이력은 불변이며 수정·삭제할 수 없습니다.' end as \"operationRule\"")
            .FROM("user_sessions us")
            .LEFT_OUTER_JOIN("korus_personnel_snapshots kps on kps.person_id = us.user_id or kps.employee_no = us.user_id")
            .LEFT_OUTER_JOIN("lateral (select termination_id, termination_type from session_termination_histories sth where sth.session_id = us.session_id order by terminated_at desc, termination_id desc limit 1) sth on true");
        if (condition.q() != null) {
            sql.WHERE("(us.session_id ilike '%' || #{q} || '%' or us.user_id ilike '%' || #{q} || '%' or us.ip_address ilike '%' || #{q} || '%' or us.session_status ilike '%' || #{q} || '%')");
        }
        if (condition.status() != null) {
            sql.WHERE("us.session_status = #{status}");
        }
        if (condition.ipAddress() != null) {
            sql.WHERE("us.ip_address = #{ipAddress}");
        }
        return sql.toString();
    }

    private static String orderBy(String sort) {
        return switch (sort == null ? "" : sort) {
            case "userId" -> "us.user_id asc, us.login_at desc";
            case "loginAt" -> "us.login_at desc, us.session_id asc";
            case "lastActivityAt" -> "us.last_activity_at desc, us.session_id asc";
            case "status", "sessionStatus" -> "us.session_status asc, us.last_activity_at desc";
            default -> "case when us.session_status = 'ACTIVE' then 0 else 1 end asc, us.last_activity_at desc, us.session_id asc";
        };
    }
}
