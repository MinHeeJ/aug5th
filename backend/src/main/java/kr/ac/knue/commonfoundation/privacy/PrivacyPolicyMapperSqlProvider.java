package kr.ac.knue.commonfoundation.privacy;

import org.apache.ibatis.jdbc.SQL;

public final class PrivacyPolicyMapperSqlProvider {

    private PrivacyPolicyMapperSqlProvider() {
    }

    public static String selectPrivacyPolicies(PrivacyPolicySearchCondition condition) {
        return baseSelect(condition)
            + " order by " + orderBy(condition.sort())
            + " limit #{size} offset #{offset}";
    }

    public static String countPrivacyPolicies(PrivacyPolicySearchCondition condition) {
        return "select count(*) from (" + baseSelect(condition) + ") counted_privacy_policies";
    }

    private static String baseSelect(PrivacyPolicySearchCondition condition) {
        SQL sql = new SQL()
            .SELECT("pfp.field_policy_id as \"fieldPolicyId\"")
            .SELECT("pfp.field_name as \"fieldName\"")
            .SELECT("pfp.privacy_grade as \"privacyGrade\"")
            .SELECT("case pfp.privacy_grade when 'PUBLIC' then '일반' when 'PERSONAL' then '개인정보' when 'SENSITIVE' then '민감정보' else pfp.privacy_grade end as \"privacyGradeName\"")
            .SELECT("pfp.encryption_enabled as \"encryptionEnabled\"")
            .SELECT("pfp.masking_rule as \"maskingRule\"")
            .SELECT("pfp.log_excluded as \"logExcluded\"")
            .SELECT("case when pfp.encryption_enabled then 'AES-256-GCM 암호화와 HMAC 검색 식별자 적용 대상입니다.' else '원문 저장 없이 마스킹 정책만 적용합니다.' end as \"policyRule\"")
            .SELECT("case when pfp.log_excluded then '감사로그에는 원문과 처리값을 제외하고 목적·결과만 기록합니다.' else '조회·출력·다운로드 처리이력을 감사로그에 기록합니다.' end as \"auditRule\"")
            .FROM("privacy_field_policies pfp");
        if (condition.q() != null) {
            sql.WHERE("(pfp.field_name ilike '%' || #{q} || '%' or pfp.privacy_grade ilike '%' || #{q} || '%' or pfp.masking_rule ilike '%' || #{q} || '%')");
        }
        if (condition.privacyGrade() != null) {
            sql.WHERE("pfp.privacy_grade = #{privacyGrade}");
        }
        if (condition.encryptionEnabled() != null) {
            sql.WHERE("pfp.encryption_enabled = #{encryptionEnabled}");
        }
        if (condition.logExcluded() != null) {
            sql.WHERE("pfp.log_excluded = #{logExcluded}");
        }
        return sql.toString();
    }

    private static String orderBy(String sort) {
        return switch (sort == null ? "" : sort) {
            case "fieldName" -> "pfp.field_name asc, pfp.field_policy_id asc";
            case "privacyGrade" -> "pfp.privacy_grade asc, pfp.field_name asc";
            default -> "pfp.field_policy_id asc";
        };
    }
}
