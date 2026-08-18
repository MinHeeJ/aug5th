package kr.ac.knue.commonfoundation.menupermission;

import org.apache.ibatis.jdbc.SQL;

public final class MenuPermissionMapperSqlProvider {

    private MenuPermissionMapperSqlProvider() {
    }

    public static String selectMenuPermissions(MenuPermissionSearchCondition condition) {
        return baseSelect(condition)
            + " order by " + orderBy(condition.sort())
            + " limit #{size} offset #{offset}";
    }

    public static String countMenuPermissions(MenuPermissionSearchCondition condition) {
        return "select count(*) from (" + baseSelect(condition) + ") counted_menu_permissions";
    }

    private static String baseSelect(MenuPermissionSearchCondition condition) {
        SQL sql = new SQL()
            .SELECT("mp.menu_permission_id as \"menuPermissionId\"")
            .SELECT("mp.target_type as \"targetType\"")
            .SELECT("mp.target_id as \"targetId\"")
            .SELECT("case mp.target_type when 'ROLE' then role.role_name when 'ORG' then org.organization_name when 'USER' then coalesce(person.name_encrypted, ua.user_id) else mp.target_id end as \"targetName\"")
            .SELECT("mp.menu_id as \"menuId\"")
            .SELECT("menu.menu_name as \"menuName\"")
            .SELECT("parent.menu_name as \"parentMenuName\"")
            .SELECT("menu.screen_id as \"screenId\"")
            .SELECT("menu.url as \"url\"")
            .SELECT("mp.allowed as \"allowed\"")
            .SELECT("case mp.target_type when 'ROLE' then '역할 권한' when 'ORG' then '조직 권한' when 'USER' then '사용자 권한' else '기타' end as \"permissionSource\"")
            .SELECT("menu.display_order as \"displayOrder\"")
            .FROM("menu_permissions mp")
            .JOIN("menus menu on menu.menu_id = mp.menu_id")
            .LEFT_OUTER_JOIN("menus parent on parent.menu_id = menu.parent_menu_id")
            .LEFT_OUTER_JOIN("roles role on role.role_code = mp.target_id and mp.target_type = 'ROLE'")
            .LEFT_OUTER_JOIN("organizations org on org.organization_code = mp.target_id and mp.target_type = 'ORG'")
            .LEFT_OUTER_JOIN("user_accounts ua on ua.user_id = mp.target_id and mp.target_type = 'USER'")
            .LEFT_OUTER_JOIN("korus_personnel_snapshots person on person.person_id = ua.user_id");
        if (condition.q() != null) {
            sql.WHERE("(mp.target_id ilike '%' || #{q} || '%' or menu.menu_name ilike '%' || #{q} || '%' or menu.screen_id ilike '%' || #{q} || '%' or coalesce(role.role_name, org.organization_name, person.name_encrypted, '') ilike '%' || #{q} || '%')");
        }
        if (condition.targetType() != null) {
            sql.WHERE("mp.target_type = #{targetType}");
        }
        if (condition.targetId() != null) {
            sql.WHERE("mp.target_id = #{targetId}");
        }
        if (condition.allowed() != null) {
            sql.WHERE("mp.allowed = #{allowed}");
        }
        return sql.toString();
    }

    private static String orderBy(String sort) {
        return switch (sort == null ? "" : sort) {
            case "targetType" -> "mp.target_type asc, mp.target_id asc, menu.display_order asc";
            case "targetId" -> "mp.target_id asc, menu.display_order asc";
            case "menuName" -> "menu.menu_name asc, mp.menu_permission_id asc";
            case "allowed" -> "mp.allowed desc, menu.display_order asc";
            default -> "menu.display_order asc, mp.menu_permission_id asc";
        };
    }
}
