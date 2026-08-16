package kr.ac.knue.commonfoundation.codegroup;

import org.apache.ibatis.jdbc.SQL;

public final class CodeGroupMapperSqlProvider {

    private CodeGroupMapperSqlProvider() {
    }

    public static String selectCodeGroups(CodeGroupSearchCondition condition) {
        return baseSelect(condition)
            + " order by " + orderBy(condition.sort())
            + " limit #{size} offset #{offset}";
    }

    public static String countCodeGroups(CodeGroupSearchCondition condition) {
        return "select count(*) from (" + baseSelect(condition) + ") counted_code_groups";
    }

    private static String baseSelect(CodeGroupSearchCondition condition) {
        SQL sql = new SQL()
            .SELECT("cg.group_id as \"groupId\"")
            .SELECT("cg.group_name as \"groupName\"")
            .SELECT("cg.description as \"description\"")
            .SELECT("cg.managing_department as \"managingDepartment\"")
            .SELECT("cg.enabled as \"enabled\"")
            .SELECT("count(cd.code_detail_id) as \"detailCount\"")
            .SELECT("count(cd.code_detail_id) filter (where cd.display_order > 0) as \"enabledDetailCount\"")
            .SELECT("'상세코드 관리에서 코드값·코드명·정렬순서 변경' as \"detailManagementRule\"")
            .FROM("code_groups cg")
            .LEFT_OUTER_JOIN("code_details cd on cd.group_id = cg.group_id");
        if (condition.q() != null) {
            sql.WHERE("(cg.group_id ilike '%' || #{q} || '%' or cg.group_name ilike '%' || #{q} || '%' or coalesce(cg.description, '') ilike '%' || #{q} || '%' or coalesce(cg.managing_department, '') ilike '%' || #{q} || '%')");
        }
        if (condition.enabled() != null) {
            sql.WHERE("cg.enabled = #{enabled}");
        }
        if (condition.managingDepartment() != null) {
            sql.WHERE("cg.managing_department = #{managingDepartment}");
        }
        sql.GROUP_BY("cg.group_id, cg.group_name, cg.description, cg.managing_department, cg.enabled");
        return sql.toString();
    }

    private static String orderBy(String sort) {
        return switch (sort == null ? "" : sort) {
            case "groupName" -> "cg.group_name asc, cg.group_id asc";
            case "managingDepartment" -> "cg.managing_department asc nulls last, cg.group_name asc";
            case "enabled" -> "cg.enabled desc, cg.group_name asc";
            default -> "cg.group_id asc";
        };
    }
}
