package kr.ac.knue.commonfoundation.codedetail;

import org.apache.ibatis.jdbc.SQL;

public final class CodeDetailMapperSqlProvider {

    private CodeDetailMapperSqlProvider() {
    }

    public static String selectCodeDetails(CodeDetailSearchCondition condition) {
        return baseSelect(condition)
            + " order by " + orderBy(condition.sort())
            + " limit #{size} offset #{offset}";
    }

    public static String countCodeDetails(CodeDetailSearchCondition condition) {
        return "select count(*) from (" + baseSelect(condition) + ") counted_code_details";
    }

    private static String baseSelect(CodeDetailSearchCondition condition) {
        SQL sql = new SQL()
            .SELECT("cd.code_detail_id as \"codeDetailId\"")
            .SELECT("cd.group_id as \"groupId\"")
            .SELECT("cg.group_name as \"groupName\"")
            .SELECT("cd.code_value as \"codeValue\"")
            .SELECT("cd.code_name as \"codeName\"")
            .SELECT("cd.parent_code_value as \"parentCodeValue\"")
            .SELECT("parent_cd.code_name as \"parentCodeName\"")
            .SELECT("cd.display_order as \"displayOrder\"")
            .SELECT("(cd.display_order > 0) as \"active\"")
            .SELECT("'그룹 내 코드값은 중복될 수 없고 정렬순서로 표시됩니다.' as \"detailUsageRule\"")
            .FROM("code_details cd")
            .JOIN("code_groups cg on cg.group_id = cd.group_id")
            .LEFT_OUTER_JOIN("code_details parent_cd on parent_cd.group_id = cd.group_id and parent_cd.code_value = cd.parent_code_value");
        if (condition.q() != null) {
            sql.WHERE("(cd.code_value ilike '%' || #{q} || '%' or cd.code_name ilike '%' || #{q} || '%' or cg.group_name ilike '%' || #{q} || '%' or coalesce(cd.parent_code_value, '') ilike '%' || #{q} || '%')");
        }
        if (condition.groupId() != null) {
            sql.WHERE("cd.group_id = #{groupId}");
        }
        if (condition.active() != null) {
            if (condition.active()) {
                sql.WHERE("cd.display_order > 0");
            } else {
                sql.WHERE("cd.display_order <= 0");
            }
        }
        return sql.toString();
    }

    private static String orderBy(String sort) {
        return switch (sort == null ? "" : sort) {
            case "groupId" -> "cd.group_id asc, cd.display_order asc, cd.code_value asc";
            case "codeName" -> "cd.code_name asc, cd.group_id asc, cd.display_order asc";
            case "displayOrder" -> "cd.display_order asc, cd.group_id asc, cd.code_value asc";
            case "active" -> "(cd.display_order > 0) desc, cd.group_id asc, cd.display_order asc";
            default -> "cd.group_id asc, cd.display_order asc, cd.code_value asc";
        };
    }
}
