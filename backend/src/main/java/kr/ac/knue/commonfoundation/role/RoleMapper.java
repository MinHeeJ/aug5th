package kr.ac.knue.commonfoundation.role;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RoleMapper {

    @SelectProvider(type = RoleMapperSqlProvider.class, method = "selectRoles")
    List<RoleListItem> selectRoles(RoleSearchCondition condition);

    @SelectProvider(type = RoleMapperSqlProvider.class, method = "countRoles")
    long countRoles(RoleSearchCondition condition);

    @Select("""
        select exists(select 1 from roles where role_code = #{roleCode})
        """)
    boolean existsRole(@Param("roleCode") String roleCode);

    @Update("""
        update roles
        set enabled = #{enabled},
            default_data_scope = #{defaultDataScope},
            purpose = #{purpose},
            grant_criteria = #{grantCriteria}
        where role_code = #{roleCode}
        """)
    int updateRoleManagementFields(
        @Param("roleCode") String roleCode,
        @Param("enabled") Boolean enabled,
        @Param("defaultDataScope") String defaultDataScope,
        @Param("purpose") String purpose,
        @Param("grantCriteria") String grantCriteria
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
