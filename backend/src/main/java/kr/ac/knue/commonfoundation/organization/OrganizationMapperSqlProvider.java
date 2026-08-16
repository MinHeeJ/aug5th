package kr.ac.knue.commonfoundation.organization;

import org.apache.ibatis.jdbc.SQL;

public final class OrganizationMapperSqlProvider {

    private OrganizationMapperSqlProvider() {
    }

    public static String selectOrganizations(OrganizationSearchCondition condition) {
        return baseSelect(condition)
            + " order by " + orderBy(condition.sort())
            + " limit #{size} offset #{offset}";
    }

    public static String countOrganizations(OrganizationSearchCondition condition) {
        return "select count(*) from (" + baseSelect(condition) + ") counted_organizations";
    }

    private static String baseSelect(OrganizationSearchCondition condition) {
        SQL sql = new SQL()
            .SELECT("org.organization_code as \"organizationCode\"")
            .SELECT("org.organization_name as \"organizationName\"")
            .SELECT("org.parent_organization_code as \"parentOrganizationCode\"")
            .SELECT("parent.organization_name as \"parentOrganizationName\"")
            .SELECT("to_char(org.valid_from, 'YYYY-MM-DD') as \"validFrom\"")
            .SELECT("to_char(org.valid_to, 'YYYY-MM-DD') as \"validTo\"")
            .SELECT("org.enabled")
            .SELECT("coalesce(children.child_count, 0) as \"childCount\"")
            .SELECT("coalesce(assignments.assigned_user_count, 0) as \"assignedUserCount\"")
            .FROM("organizations org")
            .LEFT_OUTER_JOIN("organizations parent on parent.organization_code = org.parent_organization_code")
            .LEFT_OUTER_JOIN("(select parent_organization_code, count(*) as child_count from organizations where parent_organization_code is not null group by parent_organization_code) children on children.parent_organization_code = org.organization_code")
            .LEFT_OUTER_JOIN("(select organization_code, count(distinct user_id) as assigned_user_count from position_assignments where valid_from <= current_date and (valid_to is null or valid_to >= current_date) group by organization_code) assignments on assignments.organization_code = org.organization_code");
        if (condition.q() != null) {
            sql.WHERE("(org.organization_code ilike '%' || #{q} || '%' or org.organization_name ilike '%' || #{q} || '%' or parent.organization_name ilike '%' || #{q} || '%')");
        }
        if (condition.parentOrganizationCode() != null) {
            sql.WHERE("org.parent_organization_code = #{parentOrganizationCode}");
        }
        if (condition.enabled() != null) {
            sql.WHERE("org.enabled = #{enabled}");
        }
        return sql.toString();
    }

    private static String orderBy(String sort) {
        return switch (sort == null ? "" : sort) {
            case "name" -> "org.organization_name asc, org.organization_code asc";
            case "parent" -> "parent.organization_name asc nulls first, org.organization_code asc";
            case "validFrom" -> "org.valid_from desc, org.organization_code asc";
            case "enabled" -> "org.enabled desc, org.organization_code asc";
            default -> "org.organization_code asc";
        };
    }
}
