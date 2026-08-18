package kr.ac.knue.commonfoundation.userrole;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserRoleMapper {

    @SelectProvider(type = UserRoleMapperSqlProvider.class, method = "selectUserRoles")
    List<UserRoleListItem> selectUserRoles(UserRoleSearchCondition condition);

    @SelectProvider(type = UserRoleMapperSqlProvider.class, method = "countUserRoles")
    long countUserRoles(UserRoleSearchCondition condition);

    @Select("""
        select exists(select 1 from user_roles where user_role_id = #{userRoleId})
        """)
    boolean existsUserRole(@Param("userRoleId") Long userRoleId);

    @Update("""
        update user_roles
        set valid_to = #{validTo},
            assignment_source = #{assignmentSource}
        where user_role_id = #{userRoleId}
        """)
    int updateUserRoleManagementFields(
        @Param("userRoleId") Long userRoleId,
        @Param("validTo") LocalDate validTo,
        @Param("assignmentSource") String assignmentSource
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
