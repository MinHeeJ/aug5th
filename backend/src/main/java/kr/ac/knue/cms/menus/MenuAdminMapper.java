package kr.ac.knue.cms.menus;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MenuAdminMapper {
    @Select("""
        select menu_id as "menuId", parent_menu_id as "parentMenuId", menu_level as "menuLevel",
               display_order as "displayOrder", menu_name as "menuName", screen_id as "screenId",
               case when url like '/api/admin/%' then regexp_replace(url, '^/api', '') else url end as "url",
               icon, business_division as "businessDivision", description, is_used as "isUsed"
        from menus
        where deleted_at is null
        order by case menu_level when 'MAIN' then 1 when 'MIDDLE' then 2 else 3 end, parent_menu_id nulls first, display_order, menu_name
        """)
    List<Map<String, Object>> findAllMenus();

    @Select("""
        select menu_id as "menuId", parent_menu_id as "parentMenuId", menu_level as "menuLevel",
               display_order as "displayOrder", menu_name as "menuName", screen_id as "screenId",
               case when url like '/api/admin/%' then regexp_replace(url, '^/api', '') else url end as "url",
               icon, business_division as "businessDivision", description, is_used as "isUsed"
        from menus
        where menu_id = #{menuId} and deleted_at is null
        """)
    Map<String, Object> findMenu(@Param("menuId") UUID menuId);

    @Select("select count(*) > 0 from menus where menu_id = #{menuId} and deleted_at is null")
    boolean menuExists(@Param("menuId") UUID menuId);

    @Select("select count(*) > 0 from menus where menu_id = #{parentMenuId} and deleted_at is null")
    boolean parentExists(@Param("parentMenuId") UUID parentMenuId);

    @Select("select count(*) from menus where menu_id in (${idsSql}) and parent_menu_id is not distinct from #{parentMenuId} and deleted_at is null")
    int countSiblingsInParent(@Param("parentMenuId") UUID parentMenuId, @Param("idsSql") String idsSql);

    @Update("""
        update menus
        set parent_menu_id = #{request.parentMenuId},
            display_order = #{request.displayOrder},
            before_value = after_value,
            after_value = #{afterValue},
            change_reason = #{request.changeReason},
            updated_at = now()
        where menu_id = #{menuId}
        """)
    int updateStructure(@Param("menuId") UUID menuId, @Param("request") MenuRequest request, @Param("afterValue") String afterValue);

    @Update("""
        update menus
        set menu_name = #{request.menuName},
            screen_id = #{request.screenId},
            url = case when #{request.url} like '/admin/%' then '/api' || #{request.url} else #{request.url} end,
            icon = #{request.icon},
            business_division = #{request.businessDivision},
            description = #{request.description},
            before_value = after_value,
            after_value = #{afterValue},
            change_reason = #{request.changeReason},
            updated_at = now()
        where menu_id = #{menuId}
        """)
    int updateInformation(@Param("menuId") UUID menuId, @Param("request") MenuRequest request, @Param("afterValue") String afterValue);

    @Update("""
        update menus
        set is_used = #{request.isUsed},
            before_value = after_value,
            after_value = #{afterValue},
            change_reason = #{request.changeReason},
            updated_at = now()
        where menu_id = #{menuId}
        """)
    int updateStatus(@Param("menuId") UUID menuId, @Param("request") MenuStatusRequest request, @Param("afterValue") String afterValue);

    @Update("update menus set display_order = #{displayOrder}, before_value = after_value, after_value = #{afterValue}, change_reason = #{changeReason}, updated_at = now() where menu_id = #{menuId}")
    int updateDisplayOrder(@Param("menuId") UUID menuId, @Param("displayOrder") int displayOrder,
                           @Param("afterValue") String afterValue, @Param("changeReason") String changeReason);
}
