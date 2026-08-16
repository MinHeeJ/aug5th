-- Batch result read-only vertical slice: result counts, duration, linked log, indexes, and menu seed.
ALTER TABLE batch_results ADD COLUMN IF NOT EXISTS total_count integer;
ALTER TABLE batch_results ADD COLUMN IF NOT EXISTS excluded_count integer NOT NULL DEFAULT 0;
ALTER TABLE batch_results ADD COLUMN IF NOT EXISTS duration_ms bigint;

COMMENT ON COLUMN batch_results.batch_result_id IS 'batch_results.batch_result_id 생명주기 식별자';
COMMENT ON COLUMN batch_results.batch_execution_id IS 'batch_executions.batch_execution_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN batch_results.started_at IS '배치 실행 시작 시각';
COMMENT ON COLUMN batch_results.ended_at IS '배치 실행 종료 시각. 실행 중이면 null';
COMMENT ON COLUMN batch_results.total_count IS 'BatchResultManagementService 조회 시 성공·실패·제외 건수 합계로 해석되며 배치 어댑터 완료 시 갱신';
COMMENT ON COLUMN batch_results.success_count IS '배치 어댑터 완료 시 성공 처리 건수로 갱신';
COMMENT ON COLUMN batch_results.failure_count IS '배치 어댑터 완료 시 실패 처리 건수로 갱신';
COMMENT ON COLUMN batch_results.excluded_count IS '배치 어댑터 완료 시 제외 처리 건수로 갱신';
COMMENT ON COLUMN batch_results.duration_ms IS '배치 어댑터 완료 시 started_at/ended_at 기준 소요시간으로 갱신';
COMMENT ON COLUMN batch_results.log_file_id IS 'attachment_files.attachment_id 참조 의도 (FK 미선언)';

CREATE INDEX IF NOT EXISTS ix_batch_results_execution_id ON batch_results (batch_execution_id);
CREATE INDEX IF NOT EXISTS ix_batch_results_started_at ON batch_results (started_at);
CREATE INDEX IF NOT EXISTS ix_batch_results_log_file_id ON batch_results (log_file_id);

INSERT INTO menus (menu_id, parent_menu_id, menu_name, screen_id, url, display_order)
VALUES ('M-BATCH-RESULT', 'M-OPERATIONS', '배치 결과 조회', 'SCR-BATCH-RESULT', '/admin/operations/batch-results', 120)
ON CONFLICT (menu_id) DO UPDATE SET
    parent_menu_id = EXCLUDED.parent_menu_id,
    menu_name = EXCLUDED.menu_name,
    screen_id = EXCLUDED.screen_id,
    url = EXCLUDED.url,
    display_order = EXCLUDED.display_order;

INSERT INTO menu_permissions (target_type, target_id, menu_id, allowed)
SELECT 'ROLE', role_code, 'M-BATCH-RESULT', role_code = 'R09'
FROM roles
WHERE NOT EXISTS (
    SELECT 1 FROM menu_permissions mp
    WHERE mp.target_type = 'ROLE'
      AND mp.target_id = roles.role_code
      AND mp.menu_id = 'M-BATCH-RESULT'
);

INSERT INTO function_permissions (role_code, screen_id, action_code, allowed)
SELECT role_code, 'SCR-BATCH-RESULT', action_code, role_code = 'R09'
FROM roles
CROSS JOIN (VALUES ('READ'), ('EXPORT')) AS actions(action_code)
WHERE NOT EXISTS (
    SELECT 1 FROM function_permissions fp
    WHERE fp.role_code = roles.role_code
      AND fp.screen_id = 'SCR-BATCH-RESULT'
      AND fp.action_code = actions.action_code
);

INSERT INTO attachment_files (business_key, original_name, stored_name, extension, malware_scan_result, deleted)
SELECT 'BATCH_LOG:COMMON-AUDIT-ROLLUP', 'batch-COMMON-AUDIT-ROLLUP-seed.log', 'batch-COMMON-AUDIT-ROLLUP-seed.log', 'log', 'CLEAN', false
WHERE NOT EXISTS (
    SELECT 1 FROM attachment_files
    WHERE business_key = 'BATCH_LOG:COMMON-AUDIT-ROLLUP'
      AND original_name = 'batch-COMMON-AUDIT-ROLLUP-seed.log'
);

WITH seeded_execution AS (
    SELECT batch_execution_id
    FROM batch_executions
    WHERE batch_id = 'COMMON-AUDIT-ROLLUP'
    ORDER BY batch_execution_id
    LIMIT 1
), seeded_log AS (
    SELECT attachment_id
    FROM attachment_files
    WHERE business_key = 'BATCH_LOG:COMMON-AUDIT-ROLLUP'
      AND original_name = 'batch-COMMON-AUDIT-ROLLUP-seed.log'
    ORDER BY attachment_id
    LIMIT 1
)
INSERT INTO batch_results (batch_execution_id, started_at, ended_at, total_count, success_count, failure_count, excluded_count, duration_ms, log_file_id)
SELECT seeded_execution.batch_execution_id,
       now() - interval '2 minutes',
       now() - interval '10 seconds',
       127,
       120,
       2,
       5,
       110000,
       seeded_log.attachment_id
FROM seeded_execution
CROSS JOIN seeded_log
WHERE NOT EXISTS (
    SELECT 1 FROM batch_results br
    WHERE br.batch_execution_id = seeded_execution.batch_execution_id
);
