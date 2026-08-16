package kr.ac.knue.commonfoundation.position;

import org.apache.ibatis.jdbc.SQL;

public final class PositionMapperSqlProvider {

    private PositionMapperSqlProvider() {
    }

    public static String selectPositions(PositionSearchCondition condition) {
        return baseSelect(condition)
            + " order by " + orderBy(condition.sort())
            + " limit #{size} offset #{offset}";
    }

    public static String countPositions(PositionSearchCondition condition) {
        return "select count(*) from (" + baseSelect(condition) + ") counted_positions";
    }

    private static String baseSelect(PositionSearchCondition condition) {
        SQL sql = new SQL()
            .SELECT("pos.position_id as \"positionId\"")
            .SELECT("pos.position_code as \"positionCode\"")
            .SELECT("coalesce(code.code_name, pos.position_code) as \"positionName\"")
            .SELECT("pos.user_id as \"userId\"")
            .SELECT("coalesce(person.name_encrypted, pos.user_id) as \"userName\"")
            .SELECT("person.employee_no as \"employeeNo\"")
            .SELECT("pos.organization_code as \"organizationCode\"")
            .SELECT("org.organization_name as \"organizationName\"")
            .SELECT("to_char(pos.valid_from, 'YYYY-MM-DD') as \"validFrom\"")
            .SELECT("to_char(pos.valid_to, 'YYYY-MM-DD') as \"validTo\"")
            .SELECT("(pos.valid_from <= current_date and (pos.valid_to is null or pos.valid_to >= current_date)) as active")
            .FROM("position_assignments pos")
            .LEFT_OUTER_JOIN("korus_personnel_snapshots person on person.person_id = pos.user_id")
            .LEFT_OUTER_JOIN("organizations org on org.organization_code = pos.organization_code")
            .LEFT_OUTER_JOIN("code_details code on code.group_id = 'POSITION_CODE' and code.code_value = pos.position_code");
        if (condition.q() != null) {
            sql.WHERE("(pos.position_code ilike '%' || #{q} || '%' or pos.user_id ilike '%' || #{q} || '%' or coalesce(person.name_encrypted, '') ilike '%' || #{q} || '%' or pos.organization_code ilike '%' || #{q} || '%' or coalesce(org.organization_name, '') ilike '%' || #{q} || '%')");
        }
        if (condition.positionCode() != null) {
            sql.WHERE("pos.position_code = #{positionCode}");
        }
        if (condition.organizationCode() != null) {
            sql.WHERE("pos.organization_code = #{organizationCode}");
        }
        if (condition.active() != null && condition.active()) {
            sql.WHERE("pos.valid_from <= current_date");
            sql.WHERE("(pos.valid_to is null or pos.valid_to >= current_date)");
        }
        if (condition.active() != null && !condition.active()) {
            sql.WHERE("(pos.valid_from > current_date or pos.valid_to < current_date)");
        }
        return sql.toString();
    }

    private static String orderBy(String sort) {
        return switch (sort == null ? "" : sort) {
            case "userId" -> "pos.user_id asc, pos.position_id asc";
            case "organizationCode" -> "pos.organization_code asc, pos.position_id asc";
            case "validFrom" -> "pos.valid_from desc, pos.position_id asc";
            case "active" -> "active desc, pos.position_id asc";
            default -> "pos.position_code asc, pos.position_id asc";
        };
    }
}
