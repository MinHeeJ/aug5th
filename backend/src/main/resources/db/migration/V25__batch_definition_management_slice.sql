-- Batch definition management vertical slice: schedule definition search, upsert audit trail, indexes, and seed references.
COMMENT ON COLUMN batch_definitions.batch_id IS 'batch_definitions.batch_id 생명주기 식별자';
COMMENT ON COLUMN batch_definitions.schedule IS '배치 실행주기(CRON 또는 운영 표준 주기 표현)';
COMMENT ON COLUMN batch_definitions.predecessor_batch_id IS 'batch_definitions.batch_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN batch_definitions.parameters IS 'BatchDefinitionManagementService.saveBatchDefinition 시 애플리케이션에서 검증·갱신하는 JSON 파라미터';
COMMENT ON COLUMN batch_definitions.max_runtime_seconds IS '배치 정의 저장 시 업무 담당자가 지정하는 최대 실행 허용 시간(초)';
COMMENT ON COLUMN batch_definitions.owner_id IS 'user_accounts.user_id 참조 의도 (FK 미선언)';

CREATE INDEX IF NOT EXISTS ix_batch_definitions_owner_id ON batch_definitions (owner_id);
CREATE INDEX IF NOT EXISTS ix_batch_definitions_schedule ON batch_definitions (schedule);
CREATE INDEX IF NOT EXISTS ix_batch_definitions_predecessor ON batch_definitions (predecessor_batch_id);

INSERT INTO menus (menu_id, parent_menu_id, menu_name, screen_id, url, display_order)
VALUES ('M-BATCH-DEFINITION', 'M-OPERATIONS', '배치 정의 관리', 'SCR-BATCH-DEFINITION', '/admin/operations/batch-definitions', 100)
ON CONFLICT (menu_id) DO UPDATE SET
    parent_menu_id = EXCLUDED.parent_menu_id,
    menu_name = EXCLUDED.menu_name,
    screen_id = EXCLUDED.screen_id,
    url = EXCLUDED.url,
    display_order = EXCLUDED.display_order;

INSERT INTO menu_permissions (target_type, target_id, menu_id, allowed)
SELECT 'ROLE', role_code, 'M-BATCH-DEFINITION', role_code = 'R09'
FROM roles
WHERE NOT EXISTS (
    SELECT 1 FROM menu_permissions mp
    WHERE mp.target_type = 'ROLE'
      AND mp.target_id = roles.role_code
      AND mp.menu_id = 'M-BATCH-DEFINITION'
);

INSERT INTO code_groups (group_id, group_name, description, managing_department, enabled)
VALUES ('BATCH_DEFINITION', '배치 정의', '운영 배치 정의 표시명 코드', '시스템관리자', true)
ON CONFLICT (group_id) DO UPDATE SET
    group_name = EXCLUDED.group_name,
    description = EXCLUDED.description,
    managing_department = EXCLUDED.managing_department,
    enabled = EXCLUDED.enabled;

INSERT INTO code_details (group_id, code_value, code_name, parent_code_value, display_order)
SELECT 'BATCH_DEFINITION', 'COMMON-AUDIT-ROLLUP', '감사 로그 일별 집계', NULL, 10
WHERE NOT EXISTS (
    SELECT 1 FROM code_details
    WHERE group_id = 'BATCH_DEFINITION'
      AND code_value = 'COMMON-AUDIT-ROLLUP'
);

INSERT INTO batch_definitions (batch_id, schedule, predecessor_batch_id, parameters, max_runtime_seconds, owner_id)
VALUES ('COMMON-AUDIT-ROLLUP', '0 0 * * *', NULL, '{"businessArea":"COMMON_FOUNDATION","retentionDays":365}'::jsonb, 3600, 'admin')
ON CONFLICT (batch_id) DO UPDATE SET
    schedule = EXCLUDED.schedule,
    predecessor_batch_id = EXCLUDED.predecessor_batch_id,
    parameters = EXCLUDED.parameters,
    max_runtime_seconds = EXCLUDED.max_runtime_seconds,
    owner_id = EXCLUDED.owner_id;

INSERT INTO batch_definitions (batch_id, schedule, predecessor_batch_id, parameters, max_runtime_seconds, owner_id)
VALUES ('COMMON-SESSION-EXPIRE', '*/10 * * * *', NULL, '{"businessArea":"SECURITY","idleMinutes":30}'::jsonb, 600, 'admin')
ON CONFLICT (batch_id) DO UPDATE SET
    schedule = EXCLUDED.schedule,
    predecessor_batch_id = EXCLUDED.predecessor_batch_id,
    parameters = EXCLUDED.parameters,
    max_runtime_seconds = EXCLUDED.max_runtime_seconds,
    owner_id = EXCLUDED.owner_id;
