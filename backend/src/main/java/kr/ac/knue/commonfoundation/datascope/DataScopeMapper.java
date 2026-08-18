package kr.ac.knue.commonfoundation.datascope;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface DataScopeMapper {

    @SelectProvider(type = DataScopeMapperSqlProvider.class, method = "selectDataScopes")
    List<DataScopeListItem> selectDataScopes(DataScopeSearchCondition condition);

    @SelectProvider(type = DataScopeMapperSqlProvider.class, method = "countDataScopes")
    long countDataScopes(DataScopeSearchCondition condition);

    @Select("""
        select exists(select 1 from data_scope_permissions where data_scope_id = #{dataScopeId})
        """)
    boolean existsDataScope(@Param("dataScopeId") Long dataScopeId);

    @Select("""
        select count(*) = 0
        from data_scope_permissions
        where data_scope_id = #{dataScopeId}
          and role_code = #{roleCode}
        """)
    boolean roleIdentityMismatch(@Param("dataScopeId") Long dataScopeId, @Param("roleCode") String roleCode);

    @Update("""
        update data_scope_permissions
        set scope_type = #{scopeType},
            organization_code = #{organizationCode},
            business_area = #{businessArea}
        where data_scope_id = #{dataScopeId}
        """)
    int updateDataScope(
        @Param("dataScopeId") Long dataScopeId,
        @Param("scopeType") String scopeType,
        @Param("organizationCode") String organizationCode,
        @Param("businessArea") String businessArea
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
