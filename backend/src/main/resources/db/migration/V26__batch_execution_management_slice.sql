-- Batch execution management vertical slice: manual run, stop, rerun history, indexes, and seed references.
COMMENT ON COLUMN batch_executions.batch_execution_id IS 'batch_executions.batch_execution_id 생명주기 식별자';
COMMENT ON COLUMN batch_executions.batch_id IS 'batch_definitions.batch_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN batch_executions.parameters IS 'BatchExecutionManagementService.runBatch/rerunBatch 시 애플리케이션에서 검증·갱신하는 JSON 실행 파라미터';
COMMENT ON COLUMN batch_executions.reason IS '수동실행·중지·재실행 요청 시 필수 입력 사유';
COMMENT ON COLUMN batch_executions.execution_status IS 'REQUESTED:요청|RUNNING:실행중|SUCCESS:성공|FAILED:실패|CANCELLED:중지';
COMMENT ON COLUMN batch_executions.requested_by IS 'user_accounts.user_id 참조 의도 (FK 미선언)';

CREATE INDEX IF NOT EXISTS ix_batch_executions_batch_id ON batch_executions (batch_id);
CREATE INDEX IF NOT EXISTS ix_batch_executions_status ON batch_executions (execution_status);
CREATE INDEX IF NOT EXISTS ix_batch_executions_requested_by ON batch_executions (requested_by);

INSERT INTO menus (menu_id, parent_menu_id, menu_name, screen_id, url, display_order)
VALUES ('M-BATCH-EXECUTION', 'M-OPERATIONS', '배치 실행 관리', 'SCR-BATCH-EXECUTION', '/admin/operations/batch-executions', 110)
ON CONFLICT (menu_id) DO UPDATE SET
    parent_menu_id = EXCLUDED.parent_menu_id,
    menu_name = EXCLUDED.menu_name,
    screen_id = EXCLUDED.screen_id,
    url = EXCLUDED.url,
    display_order = EXCLUDED.display_order;

INSERT INTO menu_permissions (target_type, target_id, menu_id, allowed)
SELECT 'ROLE', role_code, 'M-BATCH-EXECUTION', role_code = 'R09'
FROM roles
WHERE NOT EXISTS (
    SELECT 1 FROM menu_permissions mp
    WHERE mp.target_type = 'ROLE'
      AND mp.target_id = roles.role_code
      AND mp.menu_id = 'M-BATCH-EXECUTION'
);

INSERT INTO code_groups (group_id, group_name, description, managing_department, enabled)
VALUES ('BATCH_EXECUTION_STATUS', '배치 실행 상태', '배치 실행 관리 상태 코드', '시스템관리자', true)
ON CONFLICT (group_id) DO UPDATE SET
    group_name = EXCLUDED.group_name,
    description = EXCLUDED.description,
    managing_department = EXCLUDED.managing_department,
    enabled = EXCLUDED.enabled;

INSERT INTO code_details (group_id, code_value, code_name, parent_code_value, display_order)
SELECT 'BATCH_EXECUTION_STATUS', status_code, status_name, NULL, display_order
FROM (VALUES
    ('REQUESTED', '요청', 10),
    ('RUNNING', '실행중', 20),
    ('SUCCESS', '성공', 30),
    ('FAILED', '실패', 40),
    ('CANCELLED', '중지', 50)
) AS seed(status_code, status_name, display_order)
WHERE NOT EXISTS (
    SELECT 1 FROM code_details cd
    WHERE cd.group_id = 'BATCH_EXECUTION_STATUS'
      AND cd.code_value = seed.status_code
);

INSERT INTO batch_executions (batch_id, parameters, reason, execution_status, requested_by)
SELECT 'COMMON-AUDIT-ROLLUP', '{"businessArea":"COMMON_FOUNDATION","mode":"manual"}'::jsonb, '시드 배치 실행 이력', 'RUNNING', 'admin'
WHERE EXISTS (SELECT 1 FROM batch_definitions WHERE batch_id = 'COMMON-AUDIT-ROLLUP')
  AND NOT EXISTS (
      SELECT 1 FROM batch_executions
      WHERE batch_id = 'COMMON-AUDIT-ROLLUP'
        AND reason = '시드 배치 실행 이력'
  );
