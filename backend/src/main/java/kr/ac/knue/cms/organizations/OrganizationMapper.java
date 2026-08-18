package kr.ac.knue.cms.organizations;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrganizationMapper {
    @Select("""
        <script>
        select organization_id as "organizationId", organization_code as "organizationCode",
               organization_name as "organizationName", organization_type as "organizationType", is_used as "isUsed"
        from organizations
        where deleted_at is null
        <if test="filter != null and filter != ''">
          and (lower(organization_code) like lower(concat('%', #{filter}, '%'))
               or lower(organization_name) like lower(concat('%', #{filter}, '%')))
        </if>
        <if test="organizationCode != null and organizationCode != ''">
          and lower(organization_code) like lower(concat('%', #{organizationCode}, '%'))
        </if>
        <if test="organizationType != null and organizationType != ''">
          and organization_type = #{organizationType}
        </if>
        order by organization_code
        limit #{size} offset #{offset}
        </script>
        """)
    List<Map<String, Object>> findOrganizations(@Param("filter") String filter,
                                                @Param("organizationCode") String organizationCode,
                                                @Param("organizationType") String organizationType,
                                                @Param("size") int size,
                                                @Param("offset") int offset);

    @Select("""
        select o.organization_id as "organizationId", o.organization_code as "organizationCode",
               o.organization_name as "organizationName", o.organization_type as "organizationType", o.is_used as "isUsed",
               r.relationship_id as "relationshipId", r.parent_organization_id as "parentOrganizationId",
               r.effective_start_date as "effectiveStartDate", r.effective_end_date as "effectiveEndDate"
        from organizations o
        left join organization_relationship_history r on r.relationship_id = (
            select rr.relationship_id from organization_relationship_history rr
            where rr.organization_id = o.organization_id and rr.is_used = true
              and rr.effective_start_date <= current_date
              and (rr.effective_end_date is null or rr.effective_end_date >= current_date)
            order by rr.effective_start_date desc, rr.created_at desc limit 1
        )
        where o.deleted_at is null and o.is_used = true
        order by o.organization_code
        """)
    List<Map<String, Object>> findTreeRows();

    @Select("""
        select relationship_id as "relationshipId", organization_id as "organizationId",
               parent_organization_id as "parentOrganizationId", effective_start_date as "effectiveStartDate",
               effective_end_date as "effectiveEndDate", change_reason as "changeReason"
        from organization_relationship_history where relationship_id = #{relationshipId}
        """)
    Map<String, Object> findRelationship(@Param("relationshipId") UUID relationshipId);

    @Insert("""
        insert into organization_relationship_history
          (relationship_id, organization_id, parent_organization_id, effective_start_date, effective_end_date,
           changed_by_user_id, changed_at, change_reason, before_value, after_value, is_used, created_at, updated_at)
        values
          (#{relationshipId}, #{organizationId}, #{parentOrganizationId}, #{effectiveStartDate}, #{effectiveEndDate},
           #{changedByUserId}, now(), #{changeReason}, #{beforeValue}, #{afterValue}, true, now(), now())
        on conflict (relationship_id) do update set
          organization_id = excluded.organization_id,
          parent_organization_id = excluded.parent_organization_id,
          effective_start_date = excluded.effective_start_date,
          effective_end_date = excluded.effective_end_date,
          changed_by_user_id = excluded.changed_by_user_id,
          changed_at = now(),
          change_reason = excluded.change_reason,
          before_value = excluded.before_value,
          after_value = excluded.after_value,
          is_used = true,
          updated_at = now()
        """)
    int upsertRelationship(@Param("relationshipId") UUID relationshipId,
                           @Param("organizationId") UUID organizationId,
                           @Param("parentOrganizationId") UUID parentOrganizationId,
                           @Param("effectiveStartDate") java.time.LocalDate effectiveStartDate,
                           @Param("effectiveEndDate") java.time.LocalDate effectiveEndDate,
                           @Param("changedByUserId") UUID changedByUserId,
                           @Param("changeReason") String changeReason,
                           @Param("beforeValue") String beforeValue,
                           @Param("afterValue") String afterValue);
}
