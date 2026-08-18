package kr.ac.knue.cms.menus;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface NavigationMapper {
    @Select("""
        <script>
        with allowed_sub_menus as (
            select distinct m.menu_id
            from menus m
            join menu_permissions mp on mp.menu_id = m.menu_id
            where m.menu_level = 'SUB'
              and m.is_used = true
              and m.deleted_at is null
              and mp.target_type = 'ROLE'
              and mp.is_allowed = true
              and mp.is_used = true
              and mp.target_id in
              <foreach collection="roleCodes" item="roleCode" open="(" separator="," close=")">
                #{roleCode}
              </foreach>
        ), visible_menus as (
            select m.* from menus m where m.menu_id in (select menu_id from allowed_sub_menus)
            union
            select parent.* from menus parent
            join menus child on child.parent_menu_id = parent.menu_id
            where child.menu_id in (select menu_id from allowed_sub_menus)
              and parent.is_used = true and parent.deleted_at is null
            union
            select grand.* from menus grand
            join menus parent on parent.parent_menu_id = grand.menu_id
            join menus child on child.parent_menu_id = parent.menu_id
            where child.menu_id in (select menu_id from allowed_sub_menus)
              and grand.is_used = true and grand.deleted_at is null
        )
        select menu_id::varchar as menu_id,
               parent_menu_id::varchar as parent_menu_id,
               menu_level,
               display_order,
               menu_name,
               screen_id,
               case when url like '/api/admin/%' then regexp_replace(url, '^/api', '') else url end as url,
               icon,
               business_division
        from visible_menus
        order by
            case menu_level when 'MAIN' then 1 when 'MIDDLE' then 2 else 3 end,
            display_order,
            menu_name
        </script>
        """)
    @Results(id = "navigationMenuRowMap", value = {
        @Result(column = "menu_id", property = "menuId"),
        @Result(column = "parent_menu_id", property = "parentMenuId"),
        @Result(column = "menu_level", property = "menuLevel"),
        @Result(column = "display_order", property = "displayOrder"),
        @Result(column = "menu_name", property = "menuName"),
        @Result(column = "screen_id", property = "screenId"),
        @Result(column = "url", property = "url"),
        @Result(column = "icon", property = "icon"),
        @Result(column = "business_division", property = "businessDivision")
    })
    List<NavigationMenuRow> findVisibleMenus(@Param("roleCodes") List<String> roleCodes);
}
