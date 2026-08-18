package kr.ac.knue.cms.userroles;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserRoleMapper {
    @Select("""
        <script>
        select ur.user_role_id as "userRoleId", ur.user_id as "userId", u.login_id as "loginId",
               coalesce(k.staff_name, u.login_id) as "staffName", ur.role_code as "roleCode",
               r.role_name as "roleName", ur.assignment_type as "assignmentType",
               ur.valid_from as "validFrom", ur.valid_to as "validTo",
               ur.approved_by_user_id as "approvedByUserId", au.login_id as "approvedByLoginId",
               ur.revoked_at as "revokedAt", ur.is_used as "isUsed"
        from user_roles ur
        join users u on u.user_id = ur.user_id
        left join korus_staff_snapshot k on k.staff_id = u.korus_staff_id
        join roles r on r.role_code = ur.role_code
        left join users au on au.user_id = ur.approved_by_user_id
        where u.deleted_at is null
        <if test="userId != null">
          and ur.user_id = #{userId}
        </if>
        <if test="roleCode != null and roleCode != ''">
          and ur.role_code = #{roleCode}
        </if>
        <if test="filter != null and filter != ''">
          and (u.login_id ilike concat('%', #{filter}, '%') or k.staff_name ilike concat('%', #{filter}, '%') or ur.role_code ilike concat('%', #{filter}, '%'))
        </if>
        order by ur.user_id asc, ur.role_code asc, ur.valid_from desc nulls last, ur.created_at desc
        </script>
        """)
    List<Map<String, Object>> findUserRoles(@Param("userId") UUID userId, @Param("roleCode") String roleCode, @Param("filter") String filter);

    @Select("select count(*) > 0 from users where user_id = #{userId} and deleted_at is null")
    boolean userExists(@Param("userId") UUID userId);

    @Select("select count(*) > 0 from roles where role_code = #{roleCode} and is_used = true")
    boolean activeRoleExists(@Param("roleCode") String roleCode);

    @Select("""
        select user_role_id from user_roles
        where user_id = #{userId} and role_code = #{roleCode} and revoked_at is null
        order by created_at desc
        limit 1
        """)
    UUID findCurrentUserRoleId(@Param("userId") UUID userId, @Param("roleCode") String roleCode);

    @Update("""
        update user_roles
        set assignment_type = #{role.assignmentType},
            valid_from = #{role.validFrom},
            valid_to = #{role.validTo},
            approved_by_user_id = #{approvedByUserId},
            revoked_at = null,
            before_value = after_value,
            after_value = #{afterValue},
            change_reason = #{changeReason},
            is_used = true,
            updated_at = now()
        where user_role_id = #{userRoleId}
        """)
    int updateActiveRole(@Param("userRoleId") UUID userRoleId, @Param("role") UserRoleRequest role,
                         @Param("approvedByUserId") UUID approvedByUserId, @Param("afterValue") String afterValue,
                         @Param("changeReason") String changeReason);

    @Insert("""
        insert into user_roles (user_role_id, user_id, role_code, assignment_type, valid_from, valid_to, approved_by_user_id,
                                revoked_at, before_value, after_value, change_reason, is_used, created_at, updated_at)
        values (gen_random_uuid(), #{userId}, #{role.roleCode}, #{role.assignmentType}, #{role.validFrom}, #{role.validTo},
                #{approvedByUserId}, null, null, #{afterValue}, #{changeReason}, true, now(), now())
        """)
    void insertActiveRole(@Param("userId") UUID userId, @Param("role") UserRoleRequest role,
                          @Param("approvedByUserId") UUID approvedByUserId, @Param("afterValue") String afterValue,
                          @Param("changeReason") String changeReason);

    @Update("""
        update user_roles
        set revoked_at = coalesce(revoked_at, now()),
            before_value = after_value,
            after_value = #{afterValue},
            change_reason = #{changeReason},
            is_used = false,
            updated_at = now()
        where user_id = #{userId} and role_code = #{roleCode} and revoked_at is null
        """)
    int revokeCurrentRole(@Param("userId") UUID userId, @Param("roleCode") String roleCode,
                          @Param("afterValue") String afterValue, @Param("changeReason") String changeReason);

    @Insert("""
        insert into user_roles (user_role_id, user_id, role_code, assignment_type, valid_from, valid_to, approved_by_user_id,
                                revoked_at, before_value, after_value, change_reason, is_used, created_at, updated_at)
        values (gen_random_uuid(), #{userId}, #{role.roleCode}, #{role.assignmentType}, #{role.validFrom}, #{role.validTo},
                #{approvedByUserId}, now(), null, #{afterValue}, #{changeReason}, false, now(), now())
        """)
    void insertRevokedMarker(@Param("userId") UUID userId, @Param("role") UserRoleRequest role,
                             @Param("approvedByUserId") UUID approvedByUserId, @Param("afterValue") String afterValue,
                             @Param("changeReason") String changeReason);
}
