package kr.ac.knue.commonfoundation.functionpermission;

import org.apache.ibatis.jdbc.SQL;

public final class FunctionPermissionMapperSqlProvider {

    private FunctionPermissionMapperSqlProvider() {
    }

    public static String selectFunctionPermissions(FunctionPermissionSearchCondition condition) {
        return baseSelect(condition)
            + " order by " + orderBy(condition.sort())
            + " limit #{size} offset #{offset}";
    }

    public static String countFunctionPermissions(FunctionPermissionSearchCondition condition) {
        return "select count(*) from (" + baseSelect(condition) + ") counted_function_permissions";
    }

    private static String baseSelect(FunctionPermissionSearchCondition condition) {
        SQL sql = new SQL()
            .SELECT("fp.function_permission_id as \"functionPermissionId\"")
            .SELECT("fp.role_code as \"roleCode\"")
            .SELECT("role.role_name as \"roleName\"")
            .SELECT("fp.screen_id as \"screenId\"")
            .SELECT("coalesce(menu.menu_name, fp.screen_id) as \"screenName\"")
            .SELECT("menu.menu_id as \"menuId\"")
            .SELECT("menu.menu_name as \"menuName\"")
            .SELECT("fp.action_code as \"actionCode\"")
            .SELECT("case fp.action_code when 'READ' then '조회' when 'CREATE' then '등록' when 'UPDATE' then '수정' when 'DELETE' then '삭제' when 'VERIFY' then '확인' when 'AUTH' then '인증' when 'APPROVE' then '승인' when 'CANCEL_APPROVAL' then '승인취소' when 'PRINT' then '출력' when 'EXCEL' then '엑셀' when 'EXPORT' then '엑셀' when 'BULK' then '일괄처리' else fp.action_code end as \"actionName\"")
            .SELECT("fp.allowed as \"allowed\"")
            .SELECT("case fp.role_code when 'R09' then '시스템관리자 전체 기능' else '역할별 제한 기능' end as \"permissionScope\"")
            .SELECT("coalesce(menu.display_order, 9999) as \"displayOrder\"")
            .FROM("function_permissions fp")
            .LEFT_OUTER_JOIN("roles role on role.role_code = fp.role_code")
            .LEFT_OUTER_JOIN("menus menu on menu.screen_id = fp.screen_id");
        if (condition.q() != null) {
            sql.WHERE("(fp.role_code ilike '%' || #{q} || '%' or fp.screen_id ilike '%' || #{q} || '%' or fp.action_code ilike '%' || #{q} || '%' or coalesce(role.role_name, menu.menu_name, '') ilike '%' || #{q} || '%')");
        }
        if (condition.roleCode() != null) {
            sql.WHERE("fp.role_code = #{roleCode}");
        }
        if (condition.screenId() != null) {
            sql.WHERE("fp.screen_id = #{screenId}");
        }
        if (condition.actionCode() != null) {
            sql.WHERE("fp.action_code = #{actionCode}");
        }
        if (condition.allowed() != null) {
            sql.WHERE("fp.allowed = #{allowed}");
        }
        return sql.toString();
    }

    private static String orderBy(String sort) {
        return switch (sort == null ? "" : sort) {
            case "roleCode" -> "fp.role_code asc, coalesce(menu.display_order, 9999) asc, fp.action_code asc";
            case "screenId" -> "fp.screen_id asc, fp.action_code asc, fp.role_code asc";
            case "actionCode" -> "fp.action_code asc, fp.role_code asc, coalesce(menu.display_order, 9999) asc";
            case "allowed" -> "fp.allowed desc, coalesce(menu.display_order, 9999) asc";
            default -> "coalesce(menu.display_order, 9999) asc, fp.role_code asc, fp.action_code asc";
        };
    }
}
