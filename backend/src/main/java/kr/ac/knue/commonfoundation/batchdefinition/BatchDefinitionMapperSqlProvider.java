package kr.ac.knue.commonfoundation.batchdefinition;

import org.apache.ibatis.jdbc.SQL;

public final class BatchDefinitionMapperSqlProvider {

    private BatchDefinitionMapperSqlProvider() {
    }

    public static String selectBatchDefinitions(BatchDefinitionSearchCondition condition) {
        return baseSelect(condition)
            + " order by " + orderBy(condition.sort())
            + " limit #{size} offset #{offset}";
    }

    public static String countBatchDefinitions(BatchDefinitionSearchCondition condition) {
        return "select count(*) from (" + baseSelect(condition) + ") counted_batch_definitions";
    }

    private static String baseSelect(BatchDefinitionSearchCondition condition) {
        SQL sql = new SQL()
            .SELECT("bd.batch_id as \"batchId\"")
            .SELECT("coalesce(cd.code_name, bd.batch_id) as \"batchName\"")
            .SELECT("bd.schedule as \"schedule\"")
            .SELECT("bd.predecessor_batch_id as \"predecessorBatchId\"")
            .SELECT("predecessor.batch_id as \"predecessorBatchName\"")
            .SELECT("coalesce(bd.parameters::text, '{}') as \"parameters\"")
            .SELECT("bd.max_runtime_seconds as \"maxRuntimeSeconds\"")
            .SELECT("bd.owner_id as \"ownerId\"")
            .SELECT("coalesce(nullif(kps.name_encrypted, ''), bd.owner_id) as \"ownerName\"")
            .SELECT("'DEFINED' as \"status\"")
            .SELECT("'정의됨' as \"statusName\"")
            .SELECT("'배치 정의 화면은 즉시 실행·중지·재실행을 제공하지 않고 배치ID·실행주기·선후행·파라미터·최대실행시간·담당자 정의만 저장합니다.' as \"operationRule\"")
            .FROM("batch_definitions bd")
            .LEFT_OUTER_JOIN("batch_definitions predecessor on predecessor.batch_id = bd.predecessor_batch_id")
            .LEFT_OUTER_JOIN("korus_personnel_snapshots kps on kps.person_id = bd.owner_id or kps.employee_no = bd.owner_id")
            .LEFT_OUTER_JOIN("code_details cd on cd.group_id = 'BATCH_DEFINITION' and cd.code_value = bd.batch_id");
        if (condition.q() != null) {
            sql.WHERE("(bd.batch_id ilike '%' || #{q} || '%' or bd.schedule ilike '%' || #{q} || '%' or bd.owner_id ilike '%' || #{q} || '%' or coalesce(bd.parameters::text, '') ilike '%' || #{q} || '%')");
        }
        if (condition.ownerId() != null) {
            sql.WHERE("bd.owner_id = #{ownerId}");
        }
        if (condition.schedule() != null) {
            sql.WHERE("bd.schedule ilike '%' || #{schedule} || '%'");
        }
        return sql.toString();
    }

    private static String orderBy(String sort) {
        return switch (sort == null ? "" : sort) {
            case "batchId" -> "bd.batch_id asc";
            case "schedule" -> "bd.schedule asc, bd.batch_id asc";
            case "ownerId" -> "bd.owner_id asc, bd.batch_id asc";
            case "maxRuntimeSeconds" -> "bd.max_runtime_seconds asc, bd.batch_id asc";
            default -> "bd.batch_id asc";
        };
    }
}
