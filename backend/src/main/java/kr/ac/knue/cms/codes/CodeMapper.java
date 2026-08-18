package kr.ac.knue.cms.codes;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CodeMapper {
    @Select("""
        select code_id::text as "codeId", group_id as "groupId", code_value as "codeValue", code_name as "codeName",
               parent_code_id::text as "parentCodeId", sort_order as "sortOrder", extra_attributes::text as "extraAttributesJson",
               valid_from as "validFrom", valid_to as "validTo", is_used as "isUsed"
        from codes
        where group_id = #{groupId}
        order by sort_order asc, code_value asc
        """)
    List<Map<String, Object>> findByGroupId(@Param("groupId") String groupId);

    @Select("""
        select code_id::text as "codeId", group_id as "groupId", code_value as "codeValue", code_name as "codeName",
               parent_code_id::text as "parentCodeId", sort_order as "sortOrder", extra_attributes::text as "extraAttributesJson",
               valid_from as "validFrom", valid_to as "validTo", is_used as "isUsed"
        from codes
        where group_id = #{groupId} and code_value = #{codeValue}
        """)
    Map<String, Object> findByGroupIdAndCodeValue(@Param("groupId") String groupId, @Param("codeValue") String codeValue);

    @Select("""
        select code_id::text as "codeId", group_id as "groupId", code_value as "codeValue", code_name as "codeName",
               parent_code_id::text as "parentCodeId", sort_order as "sortOrder", extra_attributes::text as "extraAttributesJson",
               valid_from as "validFrom", valid_to as "validTo", is_used as "isUsed"
        from codes
        where code_id = #{codeId}::uuid
        """)
    Map<String, Object> findByCodeId(@Param("codeId") String codeId);

    @Select("select exists(select 1 from codes where code_id = #{codeId}::uuid and group_id = #{groupId})")
    boolean existsParentInGroup(@Param("groupId") String groupId, @Param("codeId") String codeId);

    @Insert("""
        insert into codes (group_id, code_value, code_name, parent_code_id, sort_order, extra_attributes,
                           valid_from, valid_to, is_used, before_value, after_value)
        values (#{command.groupId}, #{command.codeValue}, #{command.codeName}, #{command.parentCodeId}::uuid,
                #{command.sortOrder}, #{command.extraAttributesJson}::jsonb, #{command.validFrom}, #{command.validTo},
                coalesce(#{command.isUsed}, true), #{command.beforeValue}, #{command.afterValue})
        on conflict (group_id, code_value) do update set
            code_name = excluded.code_name,
            parent_code_id = excluded.parent_code_id,
            sort_order = excluded.sort_order,
            extra_attributes = excluded.extra_attributes,
            valid_from = excluded.valid_from,
            valid_to = excluded.valid_to,
            is_used = excluded.is_used,
            before_value = excluded.before_value,
            after_value = excluded.after_value,
            updated_at = now()
        """)
    int upsert(@Param("command") CodeSaveCommand command);
}
