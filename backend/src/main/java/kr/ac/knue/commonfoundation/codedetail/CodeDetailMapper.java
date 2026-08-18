package kr.ac.knue.commonfoundation.codedetail;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CodeDetailMapper {

    @SelectProvider(type = CodeDetailMapperSqlProvider.class, method = "selectCodeDetails")
    List<CodeDetailListItem> selectCodeDetails(CodeDetailSearchCondition condition);

    @SelectProvider(type = CodeDetailMapperSqlProvider.class, method = "countCodeDetails")
    long countCodeDetails(CodeDetailSearchCondition condition);

    @Select("""
        select exists(select 1 from code_details where code_detail_id = #{codeDetailId})
        """)
    boolean existsCodeDetail(@Param("codeDetailId") long codeDetailId);

    @Select("""
        select cd.code_detail_id as "codeDetailId",
               cd.group_id as "groupId",
               cg.group_name as "groupName",
               cd.code_value as "codeValue",
               cd.code_name as "codeName",
               cd.parent_code_value as "parentCodeValue",
               parent_cd.code_name as "parentCodeName",
               cd.display_order as "displayOrder",
               (cd.display_order > 0) as "active",
               '그룹 내 코드값은 중복될 수 없고 정렬순서로 표시됩니다.' as "detailUsageRule"
        from code_details cd
        join code_groups cg on cg.group_id = cd.group_id
        left join code_details parent_cd on parent_cd.group_id = cd.group_id and parent_cd.code_value = cd.parent_code_value
        where cd.code_detail_id = #{codeDetailId}
        """)
    CodeDetailListItem selectCodeDetail(@Param("codeDetailId") long codeDetailId);

    @Update("""
        update code_details
        set code_name = #{codeName},
            parent_code_value = #{parentCodeValue},
            display_order = #{displayOrder}
        where code_detail_id = #{codeDetailId}
        """)
    int updateCodeDetail(
        @Param("codeDetailId") long codeDetailId,
        @Param("codeName") String codeName,
        @Param("parentCodeValue") String parentCodeValue,
        @Param("displayOrder") Integer displayOrder
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
