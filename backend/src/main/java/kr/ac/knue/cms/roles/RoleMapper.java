package kr.ac.knue.cms.roles;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RoleMapper {
    @Select("""
        select role_code as "roleCode", role_name as "roleName", role_purpose as "rolePurpose",
               assignment_criteria as "assignmentCriteria", default_data_scope as "defaultDataScope",
               is_used as "isUsed"
        from roles
        order by role_code asc
        """)
    List<Map<String, Object>> findAll();

    @Select("""
        select role_code as "roleCode", role_name as "roleName", role_purpose as "rolePurpose",
               assignment_criteria as "assignmentCriteria", default_data_scope as "defaultDataScope",
               is_used as "isUsed"
        from roles
        where role_code = #{roleCode}
        """)
    Map<String, Object> findByRoleCode(@Param("roleCode") String roleCode);

    @Update("""
        update roles
        set role_name = #{request.roleName},
            role_purpose = #{request.rolePurpose},
            assignment_criteria = #{request.assignmentCriteria},
            default_data_scope = #{request.defaultDataScope},
            is_used = #{request.isUsed},
            updated_at = now()
        where role_code = #{roleCode}
        """)
    int updateRole(@Param("roleCode") String roleCode, @Param("request") RoleUpdateRequest request);
}
