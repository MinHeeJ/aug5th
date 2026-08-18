package kr.ac.knue.cms.security;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MenuPermissionMapper {
    @Select("""
        <script>
        select count(*) > 0
        from menu_permissions mp
        join menus m on m.menu_id = mp.menu_id
        where mp.target_type = 'ROLE'
          and mp.target_id in
          <foreach collection="roleCodes" item="roleCode" open="(" separator="," close=")">
            #{roleCode}
          </foreach>
          and mp.is_allowed = true and mp.is_used = true and m.is_used = true and m.deleted_at is null
          and (m.url = #{path} or #{path} like m.url || '/%')
        </script>
        """)
    boolean hasMenuPermission(@Param("roleCodes") String[] roleCodes, @Param("path") String path);

    @Select("""
        select count(*) > 0 from menus m
        where (m.url = #{path} or #{path} like m.url || '/%') and m.is_used = true and m.deleted_at is null
        """)
    boolean isKnownMenuPath(@Param("path") String path);
}
