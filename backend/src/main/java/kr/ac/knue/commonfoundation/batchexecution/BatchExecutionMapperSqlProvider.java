package kr.ac.knue.commonfoundation.batchexecution;

import org.apache.ibatis.jdbc.SQL;

public final class BatchExecutionMapperSqlProvider {

    private BatchExecutionMapperSqlProvider() {
    }

    public static String selectBatchExecutions(BatchExecutionSearchCondition condition) {
        return baseSelect(condition)
            + " order by " + orderBy(condition.sort())
            + " limit #{size} offset #{offset}";
    }

    public static String countBatchExecutions(BatchExecutionSearchCondition condition) {
        return "select count(*) from (" + baseSelect(condition) + ") counted_batch_executions";
    }

    private static String baseSelect(BatchExecutionSearchCondition condition) {
        SQL sql = new SQL()
            .SELECT("be.batch_execution_id as \"batchExecutionId\"")
            .SELECT("be.batch_id as \"batchId\"")
            .SELECT("coalesce(cd.code_name, be.batch_id) as \"batchName\"")
            .SELECT("coalesce(be.parameters::text, '{}') as \"parameters\"")
            .SELECT("be.reason as \"reason\"")
            .SELECT("be.execution_status as \"executionStatus\"")
            .SELECT("case be.execution_status when 'REQUESTED' then '요청' when 'RUNNING' then '실행중' when 'SUCCESS' then '성공' when 'FAILED' then '실패' when 'CANCELLED' then '중지' else be.execution_status end as \"executionStatusName\"")
            .SELECT("be.requested_by as \"requestedBy\"")
            .SELECT("coalesce(nullif(kps.name_encrypted, ''), be.requested_by) as \"requestedByName\"")
            .SELECT("case when be.execution_status in ('REQUESTED','RUNNING') then '중지 가능: 실행 중 배치이며 R09 사유 입력 필요' else '재실행 가능: 기존 실행 정의와 파라미터를 복사하되 원천 업무자료는 직접 수정하지 않습니다.' end as \"operationRule\"")
            .FROM("batch_executions be")
            .LEFT_OUTER_JOIN("batch_definitions bd on bd.batch_id = be.batch_id")
            .LEFT_OUTER_JOIN("code_details cd on cd.group_id = 'BATCH_DEFINITION' and cd.code_value = be.batch_id")
            .LEFT_OUTER_JOIN("korus_personnel_snapshots kps on kps.person_id = be.requested_by or kps.employee_no = be.requested_by");
        if (condition.q() != null) {
            sql.WHERE("(be.batch_id ilike '%' || #{q} || '%' or be.reason ilike '%' || #{q} || '%' or be.execution_status ilike '%' || #{q} || '%' or be.requested_by ilike '%' || #{q} || '%')");
        }
        if (condition.batchId() != null) {
            sql.WHERE("be.batch_id = #{batchId}");
        }
        if (condition.status() != null) {
            sql.WHERE("be.execution_status = #{status}");
        }
        if (condition.requestedBy() != null) {
            sql.WHERE("be.requested_by = #{requestedBy}");
        }
        return sql.toString();
    }

    private static String orderBy(String sort) {
        return switch (sort == null ? "" : sort) {
            case "batchId" -> "be.batch_id asc, be.batch_execution_id desc";
            case "status", "executionStatus" -> "be.execution_status asc, be.batch_execution_id desc";
            case "requestedBy" -> "be.requested_by asc, be.batch_execution_id desc";
            default -> "be.batch_execution_id desc";
        };
    }
}
