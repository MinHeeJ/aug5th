package kr.ac.knue.commonfoundation.batchdefinition;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;

@Mapper
public interface BatchDefinitionMapper {

    @SelectProvider(type = BatchDefinitionMapperSqlProvider.class, method = "selectBatchDefinitions")
    List<BatchDefinitionListItem> selectBatchDefinitions(BatchDefinitionSearchCondition condition);

    @SelectProvider(type = BatchDefinitionMapperSqlProvider.class, method = "countBatchDefinitions")
    long countBatchDefinitions(BatchDefinitionSearchCondition condition);

    @Select("select exists(select 1 from batch_definitions where batch_id = #{batchId})")
    boolean existsBatchDefinition(@Param("batchId") String batchId);

    @Select("select exists(select 1 from user_accounts where user_id = #{ownerId})")
    boolean existsOwner(@Param("ownerId") String ownerId);

    @Select("""
        select
            bd.batch_id as "batchId",
            coalesce(cd.code_name, bd.batch_id) as "batchName",
            bd.schedule as "schedule",
            bd.predecessor_batch_id as "predecessorBatchId",
            predecessor.batch_id as "predecessorBatchName",
            coalesce(bd.parameters::text, '{}') as "parameters",
            bd.max_runtime_seconds as "maxRuntimeSeconds",
            bd.owner_id as "ownerId",
            coalesce(nullif(kps.name_encrypted, ''), bd.owner_id) as "ownerName",
            'DEFINED' as "status",
            '정의됨' as "statusName",
            '배치 정의 화면은 즉시 실행·중지·재실행을 제공하지 않고 배치ID·실행주기·선후행·파라미터·최대실행시간·담당자 정의만 저장합니다.' as "operationRule"
        from batch_definitions bd
        left join batch_definitions predecessor on predecessor.batch_id = bd.predecessor_batch_id
        left join korus_personnel_snapshots kps on kps.person_id = bd.owner_id or kps.employee_no = bd.owner_id
        left join code_details cd on cd.group_id = 'BATCH_DEFINITION' and cd.code_value = bd.batch_id
        where bd.batch_id = #{batchId}
        """)
    BatchDefinitionListItem selectBatchDefinition(@Param("batchId") String batchId);

    @Insert("""
        insert into batch_definitions (batch_id, schedule, predecessor_batch_id, parameters, max_runtime_seconds, owner_id)
        values (#{batchId}, #{schedule}, #{predecessorBatchId}, #{parameters}::jsonb, #{maxRuntimeSeconds}, #{ownerId})
        on conflict (batch_id) do update set
            schedule = excluded.schedule,
            predecessor_batch_id = excluded.predecessor_batch_id,
            parameters = excluded.parameters,
            max_runtime_seconds = excluded.max_runtime_seconds,
            owner_id = excluded.owner_id
        """)
    void upsertBatchDefinition(
        @Param("batchId") String batchId,
        @Param("schedule") String schedule,
        @Param("predecessorBatchId") String predecessorBatchId,
        @Param("parameters") String parameters,
        @Param("maxRuntimeSeconds") int maxRuntimeSeconds,
        @Param("ownerId") String ownerId
    );

    @Insert("""
        insert into audit_logs (log_type, target_key, actor_id, before_value, after_value, result)
        values ('UPDATE', #{targetKey}, #{actorId}, #{beforeValue}::jsonb, #{afterValue}::jsonb, 'SUCCESS')
        """)
    void insertAudit(
        @Param("targetKey") String targetKey,
        @Param("actorId") String actorId,
        @Param("beforeValue") String beforeValue,
        @Param("afterValue") String afterValue
    );
}
