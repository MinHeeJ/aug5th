package kr.ac.knue.cms.permissions;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MenuPermissionAdminMapper {
    @Select("""
        <script>
        select mp.permission_id as "permissionId",
               coalesce(mp.target_type, #{targetType}) as "targetType",
               coalesce(mp.target_id, #{targetId}) as "targetId",
               m.menu_id as "menuId", m.parent_menu_id as "parentMenuId", m.menu_level as "menuLevel",
               m.display_order as "displayOrder", m.menu_name as "menuName", m.screen_id as "screenId",
               case when m.url like '/api/admin/%' then regexp_replace(m.url, '^/api', '') else m.url end as "url",
               m.icon, m.business_division as "businessDivision", m.description,
               coalesce(mp.is_allowed, false) as "isAllowed", m.is_used as "isMenuUsed"
        from menus m
        left join menu_permissions mp on mp.menu_id = m.menu_id
          and mp.is_used = true
          and mp.target_type = #{targetType}
          and mp.target_id = #{targetId}
        where m.deleted_at is null
        <if test="filter != null and filter != ''">
          and (m.menu_name ilike concat('%', #{filter}, '%') or m.screen_id ilike concat('%', #{filter}, '%') or m.url ilike concat('%', #{filter}, '%'))
        </if>
        order by case m.menu_level when 'MAIN' then 1 when 'MIDDLE' then 2 else 3 end, m.parent_menu_id nulls first, m.display_order, m.menu_name
        </script>
        """)
    List<Map<String, Object>> findMatrix(@Param("targetType") String targetType, @Param("targetId") String targetId, @Param("filter") String filter);

    @Select("select count(*) > 0 from menus where menu_id = #{menuId} and deleted_at is null")
    boolean menuExists(@Param("menuId") UUID menuId);

    @Select("select count(*) > 0 from roles where role_code = #{targetId} and is_used = true")
    boolean roleTargetExists(@Param("targetId") String targetId);

    @Select("select count(*) > 0 from organizations where organization_id::text = #{targetId} and deleted_at is null")
    boolean organizationTargetExists(@Param("targetId") String targetId);

    @Select("select count(*) > 0 from users where user_id::text = #{targetId} and deleted_at is null")
    boolean userTargetExists(@Param("targetId") String targetId);

    @Select("""
        select permission_id as "permissionId", is_allowed as "isAllowed", after_value as "afterValue"
        from menu_permissions
        where target_type = #{targetType} and target_id = #{targetId} and menu_id = #{menuId}
        """)
    Map<String, Object> findExisting(@Param("targetType") String targetType, @Param("targetId") String targetId, @Param("menuId") UUID menuId);

    @Insert("""
        insert into menu_permissions (permission_id, target_type, target_id, menu_id, is_allowed, before_value, after_value, change_reason, is_used, created_at, updated_at)
        values (#{permissionId}, #{targetType}, #{targetId}, #{item.menuId}, #{item.isAllowed}, null, #{afterValue}, #{changeReason}, true, now(), now())
        on conflict (target_type, target_id, menu_id) do update set
            is_allowed = excluded.is_allowed,
            before_value = menu_permissions.after_value,
            after_value = excluded.after_value,
            change_reason = excluded.change_reason,
            is_used = true,
            updated_at = now()
        """)
    void upsertPermission(@Param("targetType") String targetType, @Param("targetId") String targetId,
                          @Param("item") MenuPermissionItem item, @Param("permissionId") UUID permissionId,
                          @Param("afterValue") String afterValue, @Param("changeReason") String changeReason);

    @Select("""
        <script>
        select m.menu_id as "menuId", m.menu_name as "menuName", m.screen_id as "screenId",
               case when m.url like '/api/admin/%' then regexp_replace(m.url, '^/api', '') else m.url end as "url",
               bool_or(coalesce(mp.is_allowed, false)) as "isAllowed"
        from menus m
        left join menu_permissions mp on mp.menu_id = m.menu_id
          and mp.is_used = true
          and mp.target_type = 'ROLE'
          and mp.target_id in
          <foreach collection="roleCodes" item="roleCode" open="(" separator="," close=")">
            #{roleCode}
          </foreach>
        where m.deleted_at is null and m.is_used = true
        group by m.menu_id, m.menu_name, m.screen_id, m.url, m.parent_menu_id, m.display_order, m.menu_level
        order by case m.menu_level when 'MAIN' then 1 when 'MIDDLE' then 2 else 3 end, m.parent_menu_id nulls first, m.display_order, m.menu_name
        </script>
        """)
    List<Map<String, Object>> findEffectiveForRoles(@Param("roleCodes") List<String> roleCodes);
}
