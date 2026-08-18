package kr.ac.knue.commonfoundation.userrole;

import org.apache.ibatis.jdbc.SQL;

public final class UserRoleMapperSqlProvider {

    private UserRoleMapperSqlProvider() {
    }

    public static String selectUserRoles(UserRoleSearchCondition condition) {
        return baseSelect(condition)
            + " order by " + orderBy(condition.sort())
            + " limit #{size} offset #{offset}";
    }

    public static String countUserRoles(UserRoleSearchCondition condition) {
        return "select count(*) from (" + baseSelect(condition) + ") counted_user_roles";
    }

    private static String baseSelect(UserRoleSearchCondition condition) {
        SQL sql = new SQL()
            .SELECT("ur.user_role_id as \"userRoleId\"")
            .SELECT("ur.user_id as \"userId\"")
            .SELECT("coalesce(person.name_encrypted, ur.user_id) as \"userName\"")
            .SELECT("person.employee_no as \"employeeNo\"")
            .SELECT("ur.role_code as \"roleCode\"")
            .SELECT("role.role_name as \"roleName\"")
            .SELECT("to_char(ur.valid_from, 'YYYY-MM-DD') as \"validFrom\"")
            .SELECT("to_char(ur.valid_to, 'YYYY-MM-DD') as \"validTo\"")
            .SELECT("ur.approver_id as \"approverId\"")
            .SELECT("coalesce(approver_person.name_encrypted, ur.approver_id) as \"approverName\"")
            .SELECT("ur.assignment_source as \"assignmentSource\"")
            .SELECT("(ur.valid_from <= current_date and (ur.valid_to is null or ur.valid_to >= current_date)) as active")
            .FROM("user_roles ur")
            .JOIN("roles role on role.role_code = ur.role_code")
            .LEFT_OUTER_JOIN("korus_personnel_snapshots person on person.person_id = ur.user_id")
            .LEFT_OUTER_JOIN("korus_personnel_snapshots approver_person on approver_person.person_id = ur.approver_id");
        if (condition.q() != null) {
            sql.WHERE("(ur.user_id ilike '%' || #{q} || '%' or coalesce(person.name_encrypted, '') ilike '%' || #{q} || '%' or ur.role_code ilike '%' || #{q} || '%' or role.role_name ilike '%' || #{q} || '%')");
        }
        if (condition.roleCode() != null) {
            sql.WHERE("ur.role_code = #{roleCode}");
        }
        if (condition.assignmentSource() != null) {
            sql.WHERE("ur.assignment_source = #{assignmentSource}");
        }
        if (condition.active() != null && condition.active()) {
            sql.WHERE("ur.valid_from <= current_date");
            sql.WHERE("(ur.valid_to is null or ur.valid_to >= current_date)");
        }
        if (condition.active() != null && !condition.active()) {
            sql.WHERE("(ur.valid_from > current_date or ur.valid_to < current_date)");
        }
        return sql.toString();
    }

    private static String orderBy(String sort) {
        return switch (sort == null ? "" : sort) {
            case "userId" -> "ur.user_id asc, ur.user_role_id asc";
            case "roleCode" -> "ur.role_code asc, ur.user_id asc";
            case "validFrom" -> "ur.valid_from desc, ur.user_role_id asc";
            case "assignmentSource" -> "ur.assignment_source asc, ur.user_role_id asc";
            case "active" -> "active desc, ur.user_role_id asc";
            default -> "ur.user_role_id asc";
        };
    }
}
