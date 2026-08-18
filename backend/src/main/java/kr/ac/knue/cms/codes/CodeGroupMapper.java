package kr.ac.knue.cms.codes;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CodeGroupMapper {
    @Select("""
        <script>
        select group_id as "groupId", group_name as "groupName", description,
               managing_department as "managingDepartment", is_used as "isUsed"
        from code_groups
        <where>
          <if test="filter != null and filter != ''">
            (group_id ilike concat('%', #{filter}, '%')
             or group_name ilike concat('%', #{filter}, '%')
             or coalesce(description, '') ilike concat('%', #{filter}, '%')
             or coalesce(managing_department, '') ilike concat('%', #{filter}, '%'))
          </if>
        </where>
        order by group_id asc
        </script>
        """)
    List<Map<String, Object>> findAll(@Param("filter") String filter);

    @Select("""
        select group_id as "groupId", group_name as "groupName", description,
               managing_department as "managingDepartment", is_used as "isUsed"
        from code_groups
        where group_id = #{groupId}
        """)
    Map<String, Object> findByGroupId(@Param("groupId") String groupId);

    @Select("select exists(select 1 from code_groups where group_id = #{groupId})")
    boolean existsByGroupId(@Param("groupId") String groupId);

    @Insert("""
        insert into code_groups (group_id, group_name, description, managing_department, is_used)
        values (#{group.groupId}, #{group.groupName}, #{group.description}, #{group.managingDepartment}, coalesce(#{group.isUsed}, true))
        on conflict (group_id) do update set
            group_name = excluded.group_name,
            description = excluded.description,
            managing_department = excluded.managing_department,
            is_used = excluded.is_used,
            updated_at = now()
        """)
    int upsert(@Param("group") CodeGroup group);
}
