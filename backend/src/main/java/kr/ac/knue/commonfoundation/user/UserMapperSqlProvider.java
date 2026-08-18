package kr.ac.knue.commonfoundation.user;

import org.apache.ibatis.jdbc.SQL;

public final class UserMapperSqlProvider {

    private UserMapperSqlProvider() {
    }

    public static String selectUsers(UserSearchCondition condition) {
        return baseSelect(condition)
            + " order by " + orderBy(condition.sort())
            + " limit #{size} offset #{offset}";
    }

    public static String countUsers(UserSearchCondition condition) {
        return "select count(*) from (" + baseSelect(condition) + ") counted_users";
    }

    private static String baseSelect(UserSearchCondition condition) {
        SQL sql = new SQL()
            .SELECT("ua.user_id as \"userId\"")
            .SELECT("ua.enabled")
            .SELECT("ua.role_summary as \"roleSummary\"")
            .SELECT("ua.status")
            .SELECT("kps.employee_no as \"employeeNo\"")
            .SELECT("coalesce(kps.name_masked, kps.name_encrypted, ua.user_id) as \"name\"")
            .SELECT("kps.department_code as \"departmentCode\"")
            .SELECT("org.organization_name as \"departmentName\"")
            .SELECT("kps.rank_name as \"rankName\"")
            .SELECT("kps.employment_status as \"employmentStatus\"")
            .SELECT("coalesce(pos.position_summary, '-') as \"positionSummary\"")
            .SELECT("to_char(kps.retirement_date, 'YYYY-MM-DD') as \"retirementDate\"")
            .SELECT("kps.last_synced_at as \"lastSyncedAt\"")
            .FROM("user_accounts ua")
            .LEFT_OUTER_JOIN("korus_personnel_snapshots kps on kps.person_id = ua.user_id")
            .LEFT_OUTER_JOIN("organizations org on org.organization_code = kps.department_code")
            .LEFT_OUTER_JOIN("(select user_id, string_agg(position_code, ', ' order by position_code) as position_summary from position_assignments where valid_from <= current_date and (valid_to is null or valid_to >= current_date) group by user_id) pos on pos.user_id = ua.user_id");
        if (condition.q() != null) {
            sql.WHERE("(ua.user_id ilike '%' || #{q} || '%' or kps.employee_no ilike '%' || #{q} || '%' or kps.name_masked ilike '%' || #{q} || '%' or org.organization_name ilike '%' || #{q} || '%')");
        }
        if (condition.employeeNo() != null) {
            sql.WHERE("kps.employee_no = #{employeeNo}");
        }
        if (condition.name() != null) {
            sql.WHERE("kps.name_masked ilike '%' || #{name} || '%'");
        }
        if (condition.departmentCode() != null) {
            sql.WHERE("kps.department_code = #{departmentCode}");
        }
        if (condition.rankName() != null) {
            sql.WHERE("kps.rank_name = #{rankName}");
        }
        if (condition.employmentStatus() != null) {
            sql.WHERE("kps.employment_status = #{employmentStatus}");
        }
        if (condition.roleCode() != null) {
            sql.WHERE("exists (select 1 from user_roles ur where ur.user_id = ua.user_id and ur.role_code = #{roleCode} and ur.valid_from <= current_date and (ur.valid_to is null or ur.valid_to >= current_date))");
        }
        if (condition.enabled() != null) {
            sql.WHERE("ua.enabled = #{enabled}");
        }
        return sql.toString();
    }

    private static String orderBy(String sort) {
        return switch (sort == null ? "" : sort) {
            case "employeeNo" -> "kps.employee_no asc, ua.user_id asc";
            case "name" -> "kps.name_masked asc, ua.user_id asc";
            case "department" -> "org.organization_name asc, ua.user_id asc";
            case "status" -> "ua.status asc, ua.user_id asc";
            default -> "ua.user_id asc";
        };
    }
}
