package kr.ac.knue.commonfoundation.codegroup;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CodeGroupMapper {

    @SelectProvider(type = CodeGroupMapperSqlProvider.class, method = "selectCodeGroups")
    List<CodeGroupListItem> selectCodeGroups(CodeGroupSearchCondition condition);

    @SelectProvider(type = CodeGroupMapperSqlProvider.class, method = "countCodeGroups")
    long countCodeGroups(CodeGroupSearchCondition condition);

    @Select("""
        select exists(select 1 from code_groups where group_id = #{groupId})
        """)
    boolean existsCodeGroup(@Param("groupId") String groupId);

    @Insert("""
        insert into code_groups (group_id, group_name, description, managing_department, enabled)
        values (#{groupId}, #{groupName}, #{description}, #{managingDepartment}, #{enabled})
        """)
    int insertCodeGroup(
        @Param("groupId") String groupId,
        @Param("groupName") String groupName,
        @Param("description") String description,
        @Param("managingDepartment") String managingDepartment,
        @Param("enabled") Boolean enabled
    );

    @Update("""
        update code_groups
        set group_name = #{groupName},
            description = #{description},
            managing_department = #{managingDepartment},
            enabled = #{enabled}
        where group_id = #{groupId}
        """)
    int updateCodeGroup(
        @Param("groupId") String groupId,
        @Param("groupName") String groupName,
        @Param("description") String description,
        @Param("managingDepartment") String managingDepartment,
        @Param("enabled") Boolean enabled
    );

    @Insert("""
        insert into audit_logs (log_type, target_key, actor_id, before_value, after_value, result)
        values (#{logType}, #{targetKey}, #{actorId}, null, #{afterValue}::jsonb, 'SUCCESS')
        """)
    void insertAudit(
        @Param("logType") String logType,
        @Param("targetKey") String targetKey,
        @Param("actorId") String actorId,
        @Param("afterValue") String afterValue
    );
}
