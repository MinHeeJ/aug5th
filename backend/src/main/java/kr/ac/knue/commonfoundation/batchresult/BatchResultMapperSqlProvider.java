package kr.ac.knue.commonfoundation.batchresult;

import org.apache.ibatis.jdbc.SQL;

public final class BatchResultMapperSqlProvider {

    private BatchResultMapperSqlProvider() {
    }

    public static String selectBatchResults(BatchResultSearchCondition condition) {
        return baseSelect(condition)
            + " order by " + orderBy(condition.sort())
            + " limit #{size} offset #{offset}";
    }

    public static String countBatchResults(BatchResultSearchCondition condition) {
        return "select count(*) from (" + baseSelect(condition) + ") counted_batch_results";
    }

    private static String baseSelect(BatchResultSearchCondition condition) {
        SQL sql = new SQL()
            .SELECT("br.batch_result_id as \"batchResultId\"")
            .SELECT("br.batch_execution_id as \"batchExecutionId\"")
            .SELECT("be.batch_id as \"batchId\"")
            .SELECT("coalesce(cd_batch.code_name, be.batch_id) as \"batchName\"")
            .SELECT("br.started_at as \"startedAt\"")
            .SELECT("br.ended_at as \"endedAt\"")
            .SELECT("coalesce(br.total_count, br.success_count + br.failure_count + coalesce(br.excluded_count, 0)) as \"totalCount\"")
            .SELECT("br.success_count as \"successCount\"")
            .SELECT("br.failure_count as \"failureCount\"")
            .SELECT("coalesce(br.excluded_count, 0) as \"excludedCount\"")
            .SELECT("coalesce(br.duration_ms, case when br.ended_at is null then null else floor(extract(epoch from (br.ended_at - br.started_at)) * 1000)::bigint end) as \"durationMs\"")
            .SELECT("br.log_file_id as \"logFileId\"")
            .SELECT("case when br.log_file_id is null then null else coalesce(af.original_name, 'batch-' || be.batch_id || '-' || br.batch_execution_id || '.log') end as \"logFileName\"")
            .SELECT("case when br.failure_count > 0 then 'FAILED' when br.ended_at is null then 'RUNNING' else 'SUCCESS' end as \"resultStatus\"")
            .SELECT("case when br.failure_count > 0 then '실패' when br.ended_at is null then '실행중' else '성공' end as \"resultStatusName\"")
            .SELECT("case when br.log_file_id is null then '연결된 로그파일이 없습니다.' else '로그파일은 배치 실행ID ' || br.batch_execution_id || '에 연결된 파일만 조회합니다.' end as \"logAccessRule\"")
            .SELECT("'결과 조회 화면에서는 재실행하거나 실패자료·로그파일을 수정·삭제하지 않습니다.' as \"operationRule\"")
            .FROM("batch_results br")
            .JOIN("batch_executions be on be.batch_execution_id = br.batch_execution_id")
            .LEFT_OUTER_JOIN("code_details cd_batch on cd_batch.group_id = 'BATCH_DEFINITION' and cd_batch.code_value = be.batch_id")
            .LEFT_OUTER_JOIN("attachment_files af on af.attachment_id = br.log_file_id");
        if (condition.q() != null) {
            sql.WHERE("(be.batch_id ilike '%' || #{q} || '%' or cast(br.batch_execution_id as text) ilike '%' || #{q} || '%' or cast(br.batch_result_id as text) ilike '%' || #{q} || '%')");
        }
        if (condition.batchId() != null) {
            sql.WHERE("be.batch_id = #{batchId}");
        }
        if (condition.batchExecutionId() != null) {
            sql.WHERE("br.batch_execution_id = #{batchExecutionId}");
        }
        if (condition.resultStatus() != null) {
            sql.WHERE("case when br.failure_count > 0 then 'FAILED' when br.ended_at is null then 'RUNNING' else 'SUCCESS' end = #{resultStatus}");
        }
        return sql.toString();
    }

    private static String orderBy(String sort) {
        return switch (sort == null ? "" : sort) {
            case "batchId" -> "be.batch_id asc, br.batch_result_id desc";
            case "resultStatus", "status" -> "case when br.failure_count > 0 then 'FAILED' when br.ended_at is null then 'RUNNING' else 'SUCCESS' end asc, br.batch_result_id desc";
            case "startedAt" -> "br.started_at desc, br.batch_result_id desc";
            case "duration" -> "\"durationMs\" desc nulls last, br.batch_result_id desc";
            default -> "br.batch_result_id desc";
        };
    }
}
