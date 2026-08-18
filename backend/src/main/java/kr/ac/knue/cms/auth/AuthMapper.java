package kr.ac.knue.cms.auth;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AuthMapper {
    @Select("""
        select u.user_id as "userId", u.login_id as "loginId", u.password_hash as "passwordHash",
               coalesce(k.staff_name, u.login_id) as "staffName", u.is_system_enabled as "systemEnabled", u.status as "status"
        from users u
        left join korus_staff_snapshot k on k.staff_id = u.korus_staff_id
        where u.login_id = #{loginId} and u.deleted_at is null
        """)
    Map<String, Object> findAccountByLoginId(@Param("loginId") String loginId);

    @Select("""
        select ur.role_code
        from user_roles ur
        join roles r on r.role_code = ur.role_code
        where ur.user_id = #{userId} and ur.is_used = true and ur.revoked_at is null and r.is_used = true
          and (ur.valid_from is null or ur.valid_from <= current_date)
          and (ur.valid_to is null or ur.valid_to >= current_date)
        order by ur.role_code
        """)
    List<String> findActiveRoleCodes(@Param("userId") UUID userId);

    @Insert("""
        insert into user_sessions (session_id, user_id, session_token_hash, expires_at, status, is_used, created_at, updated_at)
        values (#{sessionId}, #{userId}, #{tokenHash}, #{expiresAt}, 'active', true, now(), now())
        """)
    void insertSession(@Param("sessionId") UUID sessionId, @Param("userId") UUID userId,
                       @Param("tokenHash") String tokenHash, @Param("expiresAt") LocalDateTime expiresAt);

    @Select("""
        select s.session_id as "sessionId", u.user_id as "userId", u.login_id as "loginId",
               coalesce(k.staff_name, u.login_id) as "staffName"
        from user_sessions s
        join users u on u.user_id = s.user_id
        left join korus_staff_snapshot k on k.staff_id = u.korus_staff_id
        where s.session_token_hash = #{tokenHash} and s.status = 'active' and s.is_used = true
          and s.expires_at > now() and u.is_system_enabled = true and u.status = 'ACTIVE'
        """)
    Map<String, Object> findActiveSession(@Param("tokenHash") String tokenHash);

    @Update("""
        update user_sessions set status = 'logged_out', is_used = false, updated_at = now()
        where session_token_hash = #{tokenHash} and status = 'active'
        """)
    int logout(@Param("tokenHash") String tokenHash);
}
