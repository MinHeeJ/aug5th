package kr.ac.knue.commonfoundation.menupermission;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MenuPermissionMapper {

    @SelectProvider(type = MenuPermissionMapperSqlProvider.class, method = "selectMenuPermissions")
    List<MenuPermissionListItem> selectMenuPermissions(MenuPermissionSearchCondition condition);

    @SelectProvider(type = MenuPermissionMapperSqlProvider.class, method = "countMenuPermissions")
    long countMenuPermissions(MenuPermissionSearchCondition condition);

    @Select("""
        select exists(select 1 from menu_permissions where menu_permission_id = #{menuPermissionId})
        """)
    boolean existsMenuPermission(@Param("menuPermissionId") Long menuPermissionId);

    @Select("""
        select count(*) = 0
        from menu_permissions
        where menu_permission_id = #{menuPermissionId}
          and target_type = #{targetType}
          and target_id = #{targetId}
        """)
    boolean permissionIdentityMismatch(
        @Param("menuPermissionId") Long menuPermissionId,
        @Param("targetType") String targetType,
        @Param("targetId") String targetId
    );

    @Update("""
        update menu_permissions
        set allowed = #{allowed}
        where menu_permission_id = #{menuPermissionId}
        """)
    int updateMenuPermissionAllowed(@Param("menuPermissionId") Long menuPermissionId, @Param("allowed") Boolean allowed);

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
