package kr.ac.knue.commonfoundation.session;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SessionMapper {

    @SelectProvider(type = SessionMapperSqlProvider.class, method = "selectSessions")
    List<SessionListItem> selectSessions(SessionSearchCondition condition);

    @SelectProvider(type = SessionMapperSqlProvider.class, method = "countSessions")
    long countSessions(SessionSearchCondition condition);

    @Select("select exists(select 1 from user_sessions where session_id = #{sessionId})")
    boolean existsSession(@Param("sessionId") String sessionId);

    @Select("""
        select
            us.session_id as "sessionId",
            us.user_id as "userId",
            coalesce(nullif(kps.name_encrypted, ''), us.user_id) as "userDisplayName",
            to_char(us.login_at, 'YYYY-MM-DD') || 'T' || to_char(us.login_at, 'HH24:MI:SS') as "loginAt",
            to_char(us.last_activity_at, 'YYYY-MM-DD') || 'T' || to_char(us.last_activity_at, 'HH24:MI:SS') as "lastActivityAt",
            us.ip_address as "ipAddress",
            us.session_status as "sessionStatus",
            case us.session_status when 'ACTIVE' then '활성' when 'LOGOUT' then '로그아웃' when 'IDLE_EXPIRED' then '유휴만료' when 'ABSOLUTE_EXPIRED' then '절대만료' when 'EXPIRED' then '만료' when 'TERMINATED' then '강제종료' else us.session_status end as "sessionStatusName",
            sth.termination_id as "latestTerminationId",
            sth.termination_type as "latestTerminationType",
            case when us.session_status = 'ACTIVE' then '강제종료 가능: 활성 세션이며 R09 사유 입력 필요' else '종료 이력은 불변이며 수정·삭제할 수 없습니다.' end as "operationRule"
        from user_sessions us
        left join korus_personnel_snapshots kps on kps.person_id = us.user_id or kps.employee_no = us.user_id
        left join lateral (
            select termination_id, termination_type
            from session_termination_histories sth
            where sth.session_id = us.session_id
            order by terminated_at desc, termination_id desc
            limit 1
        ) sth on true
        where us.session_id = #{sessionId}
        """)
    SessionListItem selectSession(@Param("sessionId") String sessionId);

    @Update("""
        update user_sessions
        set session_status = 'TERMINATED',
            last_activity_at = #{terminatedAt}
        where session_id = #{sessionId}
          and session_status = 'ACTIVE'
        """)
    int terminateActiveSession(@Param("sessionId") String sessionId, @Param("terminatedAt") LocalDateTime terminatedAt);

    @Insert("""
        insert into session_termination_histories (session_id, termination_type, reason, terminated_by, terminated_at)
        values (#{sessionId}, 'FORCED', #{reason}, #{terminatedBy}, #{terminatedAt})
        """)
    void insertForcedTermination(
        @Param("sessionId") String sessionId,
        @Param("reason") String reason,
        @Param("terminatedBy") String terminatedBy,
        @Param("terminatedAt") LocalDateTime terminatedAt
    );

    @Insert("""
        insert into audit_logs (log_type, target_key, actor_id, before_value, after_value, result)
        values ('UPDATE', #{targetKey}, #{actorId}, #{beforeValue}::jsonb, #{afterValue}::jsonb, 'SUCCESS')
        """)
    void insertAudit(
        @Param("targetKey") String targetKey,
        @Param("actorId") String actorId,
        @Param("beforeValue") String beforeValue,
        @Param("afterValue") String afterValue
    );
}
