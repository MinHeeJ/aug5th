package kr.ac.knue.commonfoundation.privacy;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PrivacyPolicyMapper {

    @SelectProvider(type = PrivacyPolicyMapperSqlProvider.class, method = "selectPrivacyPolicies")
    List<PrivacyPolicyListItem> selectPrivacyPolicies(PrivacyPolicySearchCondition condition);

    @SelectProvider(type = PrivacyPolicyMapperSqlProvider.class, method = "countPrivacyPolicies")
    long countPrivacyPolicies(PrivacyPolicySearchCondition condition);

    @Select("select exists(select 1 from privacy_field_policies where field_policy_id = #{fieldPolicyId})")
    boolean existsPrivacyPolicy(@Param("fieldPolicyId") Long fieldPolicyId);

    @Select("""
        select
            field_policy_id as "fieldPolicyId",
            field_name as "fieldName",
            privacy_grade as "privacyGrade",
            case privacy_grade when 'PUBLIC' then '일반' when 'PERSONAL' then '개인정보' when 'SENSITIVE' then '민감정보' else privacy_grade end as "privacyGradeName",
            encryption_enabled as "encryptionEnabled",
            masking_rule as "maskingRule",
            log_excluded as "logExcluded",
            case when encryption_enabled then 'AES-256-GCM 암호화와 HMAC 검색 식별자 적용 대상입니다.' else '원문 저장 없이 마스킹 정책만 적용합니다.' end as "policyRule",
            case when log_excluded then '감사로그에는 원문과 처리값을 제외하고 목적·결과만 기록합니다.' else '조회·출력·다운로드 처리이력을 감사로그에 기록합니다.' end as "auditRule"
        from privacy_field_policies
        where field_policy_id = #{fieldPolicyId}
        """)
    PrivacyPolicyListItem selectPrivacyPolicy(@Param("fieldPolicyId") Long fieldPolicyId);

    @Select("select exists(select 1 from privacy_field_policies where field_policy_id <> #{fieldPolicyId} and field_name = #{fieldName})")
    boolean existsDuplicateFieldName(@Param("fieldPolicyId") Long fieldPolicyId, @Param("fieldName") String fieldName);

    @Update("""
        update privacy_field_policies
        set privacy_grade = #{privacyGrade},
            encryption_enabled = #{encryptionEnabled},
            masking_rule = #{maskingRule},
            log_excluded = #{logExcluded}
        where field_policy_id = #{fieldPolicyId}
        """)
    int updatePrivacyPolicy(
        @Param("fieldPolicyId") Long fieldPolicyId,
        @Param("privacyGrade") String privacyGrade,
        @Param("encryptionEnabled") Boolean encryptionEnabled,
        @Param("maskingRule") String maskingRule,
        @Param("logExcluded") Boolean logExcluded
    );

    @Insert("""
        insert into audit_logs (log_type, target_key, actor_id, before_value, after_value, result)
        values ('UPDATE', #{targetKey}, #{actorId}, #{beforeValue}::jsonb, #{afterValue}::jsonb, 'SUCCESS')
        """)
    void insertAudit(
        @Param("targetKey") String targetKey,
        @Param("actorId") String actorId,
        @Param("beforeValue") String beforeValue,
        @Param("afterValue") String afterValue
    );
}
