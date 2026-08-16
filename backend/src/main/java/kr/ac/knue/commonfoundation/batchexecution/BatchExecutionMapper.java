package kr.ac.knue.commonfoundation.batchexecution;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface BatchExecutionMapper {

    @SelectProvider(type = BatchExecutionMapperSqlProvider.class, method = "selectBatchExecutions")
    List<BatchExecutionListItem> selectBatchExecutions(BatchExecutionSearchCondition condition);

    @SelectProvider(type = BatchExecutionMapperSqlProvider.class, method = "countBatchExecutions")
    long countBatchExecutions(BatchExecutionSearchCondition condition);

    @Select("select exists(select 1 from batch_definitions where batch_id = #{batchId})")
    boolean existsBatchDefinition(@Param("batchId") String batchId);

    @Select("select exists(select 1 from batch_executions where batch_execution_id = #{executionId})")
    boolean existsBatchExecution(@Param("executionId") long executionId);

    @Select("""
        select
            be.batch_execution_id as "batchExecutionId",
            be.batch_id as "batchId",
            coalesce(cd.code_name, be.batch_id) as "batchName",
            coalesce(be.parameters::text, '{}') as "parameters",
            be.reason as "reason",
            be.execution_status as "executionStatus",
            case be.execution_status when 'REQUESTED' then '요청' when 'RUNNING' then '실행중' when 'SUCCESS' then '성공' when 'FAILED' then '실패' when 'CANCELLED' then '중지' else be.execution_status end as "executionStatusName",
            be.requested_by as "requestedBy",
            coalesce(nullif(kps.name_encrypted, ''), be.requested_by) as "requestedByName",
            case when be.execution_status in ('REQUESTED','RUNNING') then '중지 가능: 실행 중 배치이며 R09 사유 입력 필요' else '재실행 가능: 기존 실행 정의와 파라미터를 복사하되 원천 업무자료는 직접 수정하지 않습니다.' end as "operationRule"
        from batch_executions be
        left join code_details cd on cd.group_id = 'BATCH_DEFINITION' and cd.code_value = be.batch_id
        left join korus_personnel_snapshots kps on kps.person_id = be.requested_by or kps.employee_no = be.requested_by
        where be.batch_execution_id = #{executionId}
        """)
    BatchExecutionListItem selectBatchExecution(@Param("executionId") long executionId);

    @Select("""
        insert into batch_executions (batch_id, parameters, reason, execution_status, requested_by)
        values (#{batchId}, #{parameters}::jsonb, #{reason}, #{executionStatus}, #{requestedBy})
        returning batch_execution_id
        """)
    Long insertBatchExecution(
        @Param("batchId") String batchId,
        @Param("parameters") String parameters,
        @Param("reason") String reason,
        @Param("executionStatus") String executionStatus,
        @Param("requestedBy") String requestedBy
    );

    @Update("""
        update batch_executions
        set execution_status = #{nextStatus},
            reason = #{reason}
        where batch_execution_id = #{executionId}
          and execution_status in ('REQUESTED', 'RUNNING')
        """)
    int updateRunningExecutionStatus(
        @Param("executionId") long executionId,
        @Param("nextStatus") String nextStatus,
        @Param("reason") String reason
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
