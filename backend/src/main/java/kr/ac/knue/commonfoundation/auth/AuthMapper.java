package kr.ac.knue.commonfoundation.auth;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AuthMapper {

    @Select("""
        select user_id as userId, enabled, status, password_hash as passwordHash
        from user_accounts
        where user_id = #{userId}
        """)
    LoginUserRecord findLoginUser(@Param("userId") String userId);

    @Select("""
        select role_code
        from user_roles
        where user_id = #{userId}
          and valid_from <= current_date
          and (valid_to is null or valid_to >= current_date)
        order by role_code
        """)
    List<String> findRoles(@Param("userId") String userId);

    @Select("""
        select coalesce(max(case when dsp.scope_type = 'ALL' then 'ALL' end), min(dsp.scope_type))
        from user_roles ur
        join data_scope_permissions dsp on dsp.role_code = ur.role_code
        where ur.user_id = #{userId}
          and ur.valid_from <= current_date
          and (ur.valid_to is null or ur.valid_to >= current_date)
        """)
    String findDataScope(@Param("userId") String userId);

    @Select("""
        select us.user_id
        from user_sessions us
        where us.session_id = #{sessionId}
          and us.session_status = 'ACTIVE'
        """)
    String findActiveSessionUserId(@Param("sessionId") String sessionId);

    @Insert("""
        insert into user_sessions (session_id, user_id, login_at, last_activity_at, ip_address, session_status)
        values (#{sessionId}, #{userId}, #{now}, #{now}, #{ipAddress}, 'ACTIVE')
        """)
    void insertSession(
        @Param("sessionId") String sessionId,
        @Param("userId") String userId,
        @Param("now") LocalDateTime now,
        @Param("ipAddress") String ipAddress
    );

    @Update("""
        update user_sessions
        set session_status = #{status}, last_activity_at = #{now}
        where session_id = #{sessionId}
        """)
    int updateSessionStatus(
        @Param("sessionId") String sessionId,
        @Param("status") String status,
        @Param("now") LocalDateTime now
    );

    @Insert("""
        insert into session_termination_histories (session_id, termination_type, reason, terminated_by, terminated_at)
        values (#{sessionId}, #{terminationType}, #{reason}, #{terminatedBy}, #{terminatedAt})
        """)
    void insertTermination(
        @Param("sessionId") String sessionId,
        @Param("terminationType") String terminationType,
        @Param("reason") String reason,
        @Param("terminatedBy") String terminatedBy,
        @Param("terminatedAt") LocalDateTime terminatedAt
    );

    @Insert("""
        insert into audit_logs (log_type, target_key, actor_id, before_value, after_value, result)
        values (#{logType}, #{targetKey}, #{actorId}, null, #{afterValue}::jsonb, #{result})
        """)
    void insertAudit(
        @Param("logType") String logType,
        @Param("targetKey") String targetKey,
        @Param("actorId") String actorId,
        @Param("afterValue") String afterValue,
        @Param("result") String result
    );
}
