package kr.ac.knue.commonfoundation.menu;

import org.apache.ibatis.jdbc.SQL;

public final class MenuMapperSqlProvider {

    private MenuMapperSqlProvider() {
    }

    public static String selectMenus(MenuSearchCondition condition) {
        return baseSelect(condition)
            + " order by " + orderBy(condition.sort())
            + " limit #{size} offset #{offset}";
    }

    public static String countMenus(MenuSearchCondition condition) {
        return "select count(*) from (" + baseSelect(condition) + ") counted_menus";
    }

    private static String baseSelect(MenuSearchCondition condition) {
        SQL sql = new SQL()
            .SELECT("menu.menu_id as \"menuId\"")
            .SELECT("menu.parent_menu_id as \"parentMenuId\"")
            .SELECT("parent.menu_name as \"parentMenuName\"")
            .SELECT("menu.menu_name as \"menuName\"")
            .SELECT("menu.screen_id as \"screenId\"")
            .SELECT("menu.url as \"url\"")
            .SELECT("menu.display_order as \"displayOrder\"")
            .SELECT("(select count(*) from menus child where child.parent_menu_id = menu.menu_id) as \"childCount\"")
            .SELECT("(select count(*) from menu_permissions mp where mp.menu_id = menu.menu_id and mp.allowed = true) as \"permissionCount\"")
            .SELECT("case when menu.parent_menu_id is null then '대메뉴: 하위 메뉴 그룹 및 권한 표시 기준' else '화면 표시 및 서버 메뉴 권한 판정 기준' end as \"menuUsageRule\"")
            .FROM("menus menu")
            .LEFT_OUTER_JOIN("menus parent on parent.menu_id = menu.parent_menu_id");
        if (condition.q() != null) {
            sql.WHERE("(menu.menu_id ilike '%' || #{q} || '%' or menu.menu_name ilike '%' || #{q} || '%' or menu.screen_id ilike '%' || #{q} || '%' or menu.url ilike '%' || #{q} || '%' or coalesce(parent.menu_name, '') ilike '%' || #{q} || '%')");
        }
        if (condition.parentMenuId() != null) {
            sql.WHERE("menu.parent_menu_id = #{parentMenuId}");
        }
        if (condition.screenId() != null) {
            sql.WHERE("menu.screen_id = #{screenId}");
        }
        return sql.toString();
    }

    private static String orderBy(String sort) {
        return switch (sort == null ? "" : sort) {
            case "menuName" -> "menu.menu_name asc, menu.display_order asc";
            case "screenId" -> "menu.screen_id asc, menu.display_order asc";
            case "url" -> "menu.url asc, menu.display_order asc";
            default -> "coalesce(parent.display_order, menu.display_order) asc, menu.parent_menu_id nulls first, menu.display_order asc, menu.menu_id asc";
        };
    }
}
