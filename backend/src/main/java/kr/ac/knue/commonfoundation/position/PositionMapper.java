package kr.ac.knue.commonfoundation.position;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PositionMapper {

    @SelectProvider(type = PositionMapperSqlProvider.class, method = "selectPositions")
    List<PositionListItem> selectPositions(PositionSearchCondition condition);

    @SelectProvider(type = PositionMapperSqlProvider.class, method = "countPositions")
    long countPositions(PositionSearchCondition condition);

    @Select("""
        select exists(select 1 from position_assignments where position_id = #{positionId})
        """)
    boolean existsPosition(@Param("positionId") Long positionId);

    @Update("""
        update position_assignments
        set valid_to = #{validTo}
        where position_id = #{positionId}
        """)
    int updatePositionManagementFields(
        @Param("positionId") Long positionId,
        @Param("validTo") java.time.LocalDate validTo
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
