package kr.ac.knue.commonfoundation.menu;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MenuMapper {

    @SelectProvider(type = MenuMapperSqlProvider.class, method = "selectMenus")
    List<MenuListItem> selectMenus(MenuSearchCondition condition);

    @SelectProvider(type = MenuMapperSqlProvider.class, method = "countMenus")
    long countMenus(MenuSearchCondition condition);

    @Select("""
        select exists(select 1 from menus where menu_id = #{menuId})
        """)
    boolean existsMenu(@Param("menuId") String menuId);

    @Select("""
        select exists(select 1 from menus where menu_id = #{parentMenuId})
        """)
    boolean parentExists(@Param("parentMenuId") String parentMenuId);

    @Select("""
        select count(*) > 0
        from menus
        where menu_id = #{menuId}
          and screen_id <> #{screenId}
        """)
    boolean screenIdentityMismatch(@Param("menuId") String menuId, @Param("screenId") String screenId);

    @Select("""
        select count(*) > 0
        from menus
        where menu_id <> #{menuId}
          and screen_id = #{screenId}
        """)
    boolean duplicateScreen(@Param("menuId") String menuId, @Param("screenId") String screenId);

    @Update("""
        update menus
        set parent_menu_id = #{parentMenuId},
            menu_name = #{menuName},
            url = #{url},
            display_order = #{displayOrder}
        where menu_id = #{menuId}
        """)
    int updateMenu(
        @Param("menuId") String menuId,
        @Param("parentMenuId") String parentMenuId,
        @Param("menuName") String menuName,
        @Param("url") String url,
        @Param("displayOrder") Integer displayOrder
    );

    @Insert("""
        insert into audit_logs (log_type, target_key, actor_id, before_value, after_value, result)
        values ('UPDATE', #{targetKey}, #{actorId}, null, #{afterValue}::jsonb, 'SUCCESS')
        """)
    void insertAudit(
        @Param("targetKey") String targetKey,
        @Param("actorId") String actorId,
        @Param("afterValue") String afterValue
    );
}
