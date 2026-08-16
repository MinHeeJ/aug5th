package kr.ac.knue.commonfoundation.baseyear;

import org.apache.ibatis.jdbc.SQL;

public final class BaseYearMapperSqlProvider {

    private BaseYearMapperSqlProvider() {
    }

    public static String selectBaseYears(BaseYearSearchCondition condition) {
        return baseSelect(condition)
            + " order by " + orderBy(condition.sort())
            + " limit #{size} offset #{offset}";
    }

    public static String countBaseYears(BaseYearSearchCondition condition) {
        return "select count(*) from (" + baseSelect(condition) + ") counted_base_years";
    }

    private static String baseSelect(BaseYearSearchCondition condition) {
        SQL sql = new SQL()
            .SELECT("byr.base_year as \"baseYear\"")
            .SELECT("byr.default_query_year as \"defaultQueryYear\"")
            .SELECT("byr.copy_baseline_enabled as \"copyBaselineEnabled\"")
            .SELECT("byr.reset_enabled as \"resetEnabled\"")
            .SELECT("byr.enabled as \"enabled\"")
            .SELECT("'기준연도는 4자리 연도이며 기본 조회연도는 기준연도 이하로 관리합니다.' as \"periodRule\"")
            .SELECT("'기준정보 복사 후 초기화 실행 여부를 서버에서 검증합니다.' as \"transitionRule\"")
            .FROM("base_years byr");
        if (condition.q() != null) {
            sql.WHERE("(byr.base_year ilike '%' || #{q} || '%' or byr.default_query_year ilike '%' || #{q} || '%')");
        }
        if (condition.enabled() != null) {
            sql.WHERE("byr.enabled = #{enabled}");
        }
        return sql.toString();
    }

    private static String orderBy(String sort) {
        return switch (sort == null ? "" : sort) {
            case "defaultQueryYear" -> "byr.default_query_year desc, byr.base_year desc";
            case "enabled" -> "byr.enabled desc, byr.base_year desc";
            default -> "byr.base_year desc";
        };
    }
}
