package kr.ac.knue.commonfoundation.systemconfig;

import org.apache.ibatis.jdbc.SQL;

public final class SystemConfigurationMapperSqlProvider {

    private SystemConfigurationMapperSqlProvider() {
    }

    public static String selectSystemConfigurations(SystemConfigurationSearchCondition condition) {
        return baseSelect(condition)
            + " order by " + orderBy(condition.sort())
            + " limit #{size} offset #{offset}";
    }

    public static String countSystemConfigurations(SystemConfigurationSearchCondition condition) {
        return "select count(*) from (" + baseSelect(condition) + ") counted_system_configurations";
    }

    private static String baseSelect(SystemConfigurationSearchCondition condition) {
        SQL sql = new SQL()
            .SELECT("sc.config_key as \"configKey\"")
            .SELECT("sc.config_value as \"configValue\"")
            .SELECT("sc.unit as \"unit\"")
            .SELECT("sc.value_range as \"valueRange\"")
            .SELECT("sc.enabled as \"enabled\"")
            .SELECT("'전체 사용자 공통 적용' as \"applyScope\"")
            .SELECT("'설정값은 항목별 단위와 값 범위를 서버에서 검증합니다.' as \"validationRule\"")
            .FROM("system_configurations sc");
        if (condition.q() != null) {
            sql.WHERE("(sc.config_key ilike '%' || #{q} || '%' or sc.config_value ilike '%' || #{q} || '%' or sc.unit ilike '%' || #{q} || '%' or coalesce(sc.value_range, '') ilike '%' || #{q} || '%')");
        }
        if (condition.enabled() != null) {
            sql.WHERE("sc.enabled = #{enabled}");
        }
        return sql.toString();
    }

    private static String orderBy(String sort) {
        return switch (sort == null ? "" : sort) {
            case "configKey" -> "sc.config_key asc";
            case "unit" -> "sc.unit asc, sc.config_key asc";
            case "enabled" -> "sc.enabled desc, sc.config_key asc";
            default -> "sc.config_key asc";
        };
    }
}
