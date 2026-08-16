package kr.ac.knue.commonfoundation.datascope;

import org.apache.ibatis.jdbc.SQL;

public final class DataScopeMapperSqlProvider {

    private DataScopeMapperSqlProvider() {
    }

    public static String selectDataScopes(DataScopeSearchCondition condition) {
        return baseSelect(condition)
            + " order by " + orderBy(condition.sort())
            + " limit #{size} offset #{offset}";
    }

    public static String countDataScopes(DataScopeSearchCondition condition) {
        return "select count(*) from (" + baseSelect(condition) + ") counted_data_scopes";
    }

    private static String baseSelect(DataScopeSearchCondition condition) {
        SQL sql = new SQL()
            .SELECT("dsp.data_scope_id as \"dataScopeId\"")
            .SELECT("dsp.role_code as \"roleCode\"")
            .SELECT("role.role_name as \"roleName\"")
            .SELECT("dsp.scope_type as \"scopeType\"")
            .SELECT("case dsp.scope_type when 'SELF' then '본인' when 'DEPARTMENT' then '소속학과' when 'COLLEGE' then '단과대학' when 'BUSINESS' then '담당업무' when 'ALL' then '전체' else dsp.scope_type end as \"scopeName\"")
            .SELECT("dsp.organization_code as \"organizationCode\"")
            .SELECT("org.organization_name as \"organizationName\"")
            .SELECT("dsp.business_area as \"businessArea\"")
            .SELECT("case dsp.business_area when 'COMMON_FOUNDATION' then '공통기능 전체' when 'EVALUATION' then '교수업적평가' when 'RESEARCH' then '연구·산학' else dsp.business_area end as \"businessAreaName\"")
            .SELECT("case dsp.scope_type when 'SELF' then '로그인 사용자 본인 자료만 서버 조회조건에 강제' when 'DEPARTMENT' then '소속학과 조직 범위로 서버 조회조건 강제' when 'COLLEGE' then '단과대학 조직 범위로 서버 조회조건 강제' when 'BUSINESS' then '담당업무 영역으로 서버 조회조건 강제' when 'ALL' then '서버 조회조건 전체 범위 강제' else '사용자 정의 데이터 범위 강제' end as \"enforcementRule\"")
            .SELECT("case dsp.role_code when 'R09' then 9 when 'R08' then 8 when 'R07' then 7 when 'R06' then 6 when 'R05' then 5 when 'R04' then 4 when 'R03' then 3 when 'R02' then 2 when 'R01' then 1 else 99 end as \"displayOrder\"")
            .FROM("data_scope_permissions dsp")
            .LEFT_OUTER_JOIN("roles role on role.role_code = dsp.role_code")
            .LEFT_OUTER_JOIN("organizations org on org.organization_code = dsp.organization_code");
        if (condition.q() != null) {
            sql.WHERE("(dsp.role_code ilike '%' || #{q} || '%' or dsp.scope_type ilike '%' || #{q} || '%' or coalesce(dsp.organization_code, '') ilike '%' || #{q} || '%' or coalesce(dsp.business_area, '') ilike '%' || #{q} || '%' or coalesce(role.role_name, org.organization_name, '') ilike '%' || #{q} || '%')");
        }
        if (condition.roleCode() != null) {
            sql.WHERE("dsp.role_code = #{roleCode}");
        }
        if (condition.scopeType() != null) {
            sql.WHERE("dsp.scope_type = #{scopeType}");
        }
        if (condition.organizationCode() != null) {
            sql.WHERE("dsp.organization_code = #{organizationCode}");
        }
        if (condition.businessArea() != null) {
            sql.WHERE("dsp.business_area = #{businessArea}");
        }
        return sql.toString();
    }

    private static String orderBy(String sort) {
        return switch (sort == null ? "" : sort) {
            case "roleCode" -> "dsp.role_code asc, dsp.scope_type asc";
            case "scopeType" -> "dsp.scope_type asc, dsp.role_code asc";
            case "organizationCode" -> "dsp.organization_code asc nulls last, dsp.role_code asc";
            case "businessArea" -> "dsp.business_area asc nulls last, dsp.role_code asc";
            default -> "case dsp.role_code when 'R09' then 1 when 'R08' then 2 when 'R07' then 3 when 'R06' then 4 when 'R05' then 5 when 'R04' then 6 when 'R03' then 7 when 'R02' then 8 when 'R01' then 9 else 99 end asc, dsp.scope_type asc";
        };
    }
}
