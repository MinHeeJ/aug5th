package kr.ac.knue.commonfoundation.role;

import org.apache.ibatis.jdbc.SQL;

public final class RoleMapperSqlProvider {

    private RoleMapperSqlProvider() {
    }

    public static String selectRoles(RoleSearchCondition condition) {
        return baseSelect(condition)
            + " order by " + orderBy(condition.sort())
            + " limit #{size} offset #{offset}";
    }

    public static String countRoles(RoleSearchCondition condition) {
        return "select count(*) from (" + baseSelect(condition) + ") counted_roles";
    }

    private static String baseSelect(RoleSearchCondition condition) {
        SQL sql = new SQL()
            .SELECT("r.role_code as \"roleCode\"")
            .SELECT("r.role_name as \"roleName\"")
            .SELECT("r.purpose as \"purpose\"")
            .SELECT("r.grant_criteria as \"grantCriteria\"")
            .SELECT("r.default_data_scope as \"defaultDataScope\"")
            .SELECT("r.enabled as enabled")
            .SELECT("count(distinct ur.user_role_id) as \"assignedUserCount\"")
            .SELECT("count(distinct mp.menu_permission_id) filter (where mp.allowed) as \"menuPermissionCount\"")
            .SELECT("count(distinct fp.function_permission_id) filter (where fp.allowed) as \"functionPermissionCount\"")
            .FROM("roles r")
            .LEFT_OUTER_JOIN("user_roles ur on ur.role_code = r.role_code and ur.valid_from <= current_date and (ur.valid_to is null or ur.valid_to >= current_date)")
            .LEFT_OUTER_JOIN("menu_permissions mp on mp.target_type = 'ROLE' and mp.target_id = r.role_code")
            .LEFT_OUTER_JOIN("function_permissions fp on fp.role_code = r.role_code");
        if (condition.q() != null) {
            sql.WHERE("(r.role_code ilike '%' || #{q} || '%' or r.role_name ilike '%' || #{q} || '%' or coalesce(r.purpose, '') ilike '%' || #{q} || '%' or coalesce(r.grant_criteria, '') ilike '%' || #{q} || '%')");
        }
        if (condition.enabled() != null) {
            sql.WHERE("r.enabled = #{enabled}");
        }
        if (condition.defaultDataScope() != null) {
            sql.WHERE("r.default_data_scope = #{defaultDataScope}");
        }
        sql.GROUP_BY("r.role_code, r.role_name, r.purpose, r.grant_criteria, r.default_data_scope, r.enabled");
        return sql.toString();
    }

    private static String orderBy(String sort) {
        return switch (sort == null ? "" : sort) {
            case "roleName" -> "r.role_name asc, r.role_code asc";
            case "defaultDataScope" -> "r.default_data_scope asc, r.role_code asc";
            case "enabled" -> "r.enabled desc, r.role_code asc";
            case "assignedUserCount" -> "\"assignedUserCount\" desc, r.role_code asc";
            default -> "r.role_code asc";
        };
    }
}
