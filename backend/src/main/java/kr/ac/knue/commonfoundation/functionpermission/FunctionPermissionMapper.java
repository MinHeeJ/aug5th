package kr.ac.knue.commonfoundation.functionpermission;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface FunctionPermissionMapper {

    @SelectProvider(type = FunctionPermissionMapperSqlProvider.class, method = "selectFunctionPermissions")
    List<FunctionPermissionListItem> selectFunctionPermissions(FunctionPermissionSearchCondition condition);

    @SelectProvider(type = FunctionPermissionMapperSqlProvider.class, method = "countFunctionPermissions")
    long countFunctionPermissions(FunctionPermissionSearchCondition condition);

    @Select("""
        select exists(select 1 from function_permissions where function_permission_id = #{functionPermissionId})
        """)
    boolean existsFunctionPermission(@Param("functionPermissionId") Long functionPermissionId);

    @Select("""
        select count(*) = 0
        from function_permissions
        where function_permission_id = #{functionPermissionId}
          and role_code = #{roleCode}
          and screen_id = #{screenId}
          and action_code = #{actionCode}
        """)
    boolean permissionIdentityMismatch(
        @Param("functionPermissionId") Long functionPermissionId,
        @Param("roleCode") String roleCode,
        @Param("screenId") String screenId,
        @Param("actionCode") String actionCode
    );

    @Update("""
        update function_permissions
        set allowed = #{allowed}
        where function_permission_id = #{functionPermissionId}
        """)
    int updateFunctionPermissionAllowed(@Param("functionPermissionId") Long functionPermissionId, @Param("allowed") Boolean allowed);

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
