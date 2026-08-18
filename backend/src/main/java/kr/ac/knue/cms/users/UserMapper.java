package kr.ac.knue.cms.users;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {
    @Select("""
        <script>
        select u.user_id as "userId",
               u.login_id as "loginId",
               k.staff_id as "staffId",
               k.staff_name as "staffName",
               k.organization_code as "organizationCode",
               k.rank_title as "rankTitle",
               k.employment_status as "employmentStatus",
               k.position_title as "positionTitle",
               k.retirement_date as "retirementDate",
               k.last_synced_at as "lastSyncedAt",
               u.is_system_enabled as "isSystemEnabled"
        from users u
        left join korus_staff_snapshot k on k.staff_id = u.korus_staff_id
        where u.deleted_at is null
        <if test="filter != null and filter != ''">
          and (
            lower(u.login_id) like lower(concat('%', #{filter}, '%'))
            or lower(coalesce(k.staff_id, '')) like lower(concat('%', #{filter}, '%'))
            or lower(coalesce(k.staff_name, '')) like lower(concat('%', #{filter}, '%'))
            or lower(coalesce(k.organization_code, '')) like lower(concat('%', #{filter}, '%'))
            or lower(coalesce(k.rank_title, '')) like lower(concat('%', #{filter}, '%'))
            or lower(coalesce(k.employment_status, '')) like lower(concat('%', #{filter}, '%'))
          )
        </if>
        <if test="staffId != null and staffId != ''">and lower(coalesce(k.staff_id, '')) like lower(concat('%', #{staffId}, '%'))</if>
        <if test="staffName != null and staffName != ''">and lower(coalesce(k.staff_name, '')) like lower(concat('%', #{staffName}, '%'))</if>
        <if test="organizationCode != null and organizationCode != ''">and lower(coalesce(k.organization_code, '')) like lower(concat('%', #{organizationCode}, '%'))</if>
        <if test="rankTitle != null and rankTitle != ''">and lower(coalesce(k.rank_title, '')) like lower(concat('%', #{rankTitle}, '%'))</if>
        <if test="employmentStatus != null and employmentStatus != ''">and k.employment_status = #{employmentStatus}</if>
        <if test="systemEnabled != null">and u.is_system_enabled = #{systemEnabled}</if>
        <if test="roleCode != null and roleCode != ''">
          and exists (select 1 from user_roles ur where ur.user_id = u.user_id and ur.role_code = #{roleCode}
            and ur.is_used = true and ur.revoked_at is null
            and (ur.valid_from is null or ur.valid_from &lt;= current_date)
            and (ur.valid_to is null or ur.valid_to &gt;= current_date))
        </if>
        order by k.staff_id nulls last, u.login_id
        limit #{size} offset #{offset}
        </script>
        """)
    List<Map<String, Object>> findUsers(@Param("filter") String filter,
                                        @Param("staffId") String staffId,
                                        @Param("staffName") String staffName,
                                        @Param("organizationCode") String organizationCode,
                                        @Param("rankTitle") String rankTitle,
                                        @Param("employmentStatus") String employmentStatus,
                                        @Param("roleCode") String roleCode,
                                        @Param("systemEnabled") Boolean systemEnabled,
                                        @Param("size") int size,
                                        @Param("offset") int offset);

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

    @Select("""
        select u.user_id as "userId", u.login_id as "loginId", u.is_system_enabled as "isSystemEnabled",
               k.staff_id as "staffId", k.staff_name as "staffName", k.organization_code as "organizationCode",
               k.rank_title as "rankTitle", k.employment_status as "employmentStatus", k.position_title as "positionTitle",
               k.retirement_date as "retirementDate", k.last_synced_at as "lastSyncedAt"
        from users u left join korus_staff_snapshot k on k.staff_id = u.korus_staff_id
        where u.user_id = #{userId} and u.deleted_at is null
        """)
    Map<String, Object> findUser(@Param("userId") UUID userId);

    @Update("update users set is_system_enabled = #{enabled}, status = case when #{enabled} then 'ACTIVE' else 'DISABLED' end, updated_at = now() where user_id = #{userId}")
    int updateSystemEnabled(@Param("userId") UUID userId, @Param("enabled") boolean enabled);

    @Update("""
        update user_roles
        set is_used = false, revoked_at = coalesce(revoked_at, now()), before_value = role_code,
            after_value = 'revoked', change_reason = #{changeReason}, updated_at = now()
        where user_id = #{userId} and is_used = true
        """)
    int revokeManualRoles(@Param("userId") UUID userId, @Param("changeReason") String changeReason);

    @Insert("""
        insert into user_roles (user_id, role_code, assignment_type, valid_from, approved_by_user_id, before_value, after_value, change_reason, is_used)
        values (#{userId}, #{roleCode}, 'MANUAL', current_date, #{approvedByUserId}, null, #{roleCode}, #{changeReason}, true)
        """)
    int insertManualRole(@Param("userId") UUID userId, @Param("roleCode") String roleCode,
                         @Param("approvedByUserId") UUID approvedByUserId, @Param("changeReason") String changeReason);
}
