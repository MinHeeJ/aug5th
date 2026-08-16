package kr.ac.knue.commonfoundation.batchresult;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;

@Mapper
public interface BatchResultMapper {

    @SelectProvider(type = BatchResultMapperSqlProvider.class, method = "selectBatchResults")
    List<BatchResultListItem> selectBatchResults(BatchResultSearchCondition condition);

    @SelectProvider(type = BatchResultMapperSqlProvider.class, method = "countBatchResults")
    long countBatchResults(BatchResultSearchCondition condition);

    @Select("select exists(select 1 from batch_results where batch_result_id = #{batchResultId})")
    boolean existsBatchResult(@Param("batchResultId") long batchResultId);

    @Select("""
        select
            br.batch_result_id as "batchResultId",
            br.batch_execution_id as "batchExecutionId",
            be.batch_id as "batchId",
            coalesce(cd_batch.code_name, be.batch_id) as "batchName",
            br.started_at as "startedAt",
            br.ended_at as "endedAt",
            coalesce(br.total_count, br.success_count + br.failure_count + coalesce(br.excluded_count, 0)) as "totalCount",
            br.success_count as "successCount",
            br.failure_count as "failureCount",
            coalesce(br.excluded_count, 0) as "excludedCount",
            coalesce(br.duration_ms, case when br.ended_at is null then null else floor(extract(epoch from (br.ended_at - br.started_at)) * 1000)::bigint end) as "durationMs",
            br.log_file_id as "logFileId",
            case when br.log_file_id is null then null else coalesce(af.original_name, 'batch-' || be.batch_id || '-' || br.batch_execution_id || '.log') end as "logFileName",
            case when br.failure_count > 0 then 'FAILED' when br.ended_at is null then 'RUNNING' else 'SUCCESS' end as "resultStatus",
            case when br.failure_count > 0 then '실패' when br.ended_at is null then '실행중' else '성공' end as "resultStatusName",
            case when br.log_file_id is null then '연결된 로그파일이 없습니다.' else '로그파일은 배치 실행ID ' || br.batch_execution_id || '에 연결된 파일만 조회합니다.' end as "logAccessRule",
            '결과 조회 화면에서는 재실행하거나 실패자료·로그파일을 수정·삭제하지 않습니다.' as "operationRule"
        from batch_results br
        join batch_executions be on be.batch_execution_id = br.batch_execution_id
        left join code_details cd_batch on cd_batch.group_id = 'BATCH_DEFINITION' and cd_batch.code_value = be.batch_id
        left join attachment_files af on af.attachment_id = br.log_file_id
        where br.batch_result_id = #{batchResultId}
        """)
    BatchResultListItem selectBatchResult(@Param("batchResultId") long batchResultId);

    @Insert("""
        insert into audit_logs (log_type, target_key, actor_id, before_value, after_value, result)
        values ('READ', #{targetKey}, #{actorId}, #{beforeValue}::jsonb, #{afterValue}::jsonb, 'SUCCESS')
        """)
    void insertAudit(
        @Param("targetKey") String targetKey,
        @Param("actorId") String actorId,
        @Param("beforeValue") String beforeValue,
        @Param("afterValue") String afterValue
    );
}
