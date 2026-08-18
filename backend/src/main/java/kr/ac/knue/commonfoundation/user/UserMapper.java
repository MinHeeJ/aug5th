package kr.ac.knue.commonfoundation.user;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {

    @SelectProvider(type = UserMapperSqlProvider.class, method = "selectUsers")
    List<UserListItem> selectUsers(UserSearchCondition condition);

    @SelectProvider(type = UserMapperSqlProvider.class, method = "countUsers")
    long countUsers(UserSearchCondition condition);

    @Select("""
        select exists(select 1 from user_accounts where user_id = #{userId})
        """)
    boolean existsUser(@Param("userId") String userId);

    @Update("""
        update user_accounts
        set enabled = #{enabled}, status = #{status}, role_summary = #{roleSummary}, updated_at = #{now}
        where user_id = #{userId}
        """)
    int updateUserManagementFields(
        @Param("userId") String userId,
        @Param("enabled") Boolean enabled,
        @Param("status") String status,
        @Param("roleSummary") String roleSummary,
        @Param("now") LocalDateTime now
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
