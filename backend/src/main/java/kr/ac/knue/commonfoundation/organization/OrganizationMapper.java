package kr.ac.knue.commonfoundation.organization;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OrganizationMapper {

    @SelectProvider(type = OrganizationMapperSqlProvider.class, method = "selectOrganizations")
    List<OrganizationListItem> selectOrganizations(OrganizationSearchCondition condition);

    @SelectProvider(type = OrganizationMapperSqlProvider.class, method = "countOrganizations")
    long countOrganizations(OrganizationSearchCondition condition);

    @Select("""
        select exists(select 1 from organizations where organization_code = #{organizationCode})
        """)
    boolean existsOrganization(@Param("organizationCode") String organizationCode);

    @Update("""
        update organizations
        set enabled = #{enabled}, valid_to = #{validTo}
        where organization_code = #{organizationCode}
        """)
    int updateOrganizationManagementFields(
        @Param("organizationCode") String organizationCode,
        @Param("enabled") Boolean enabled,
        @Param("validTo") java.time.LocalDate validTo
    );

    @Insert("""
        insert into audit_logs (log_type, target_key, actor_id, before_value, after_value, result)
        values ('UPDATE', #{targetKey}, #{actorId}, null, #{afterValue}::jsonb, 'SUCCESS')
        """)
    void insertAudit(
        @Param("targetKey") String targetKey,
        @Param("actorId") String actorId,
        @Param("afterValue") String afterValue
    );
}
