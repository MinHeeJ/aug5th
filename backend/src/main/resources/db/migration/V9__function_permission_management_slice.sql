-- Function-permission management vertical slice: indexes, comments, and seed references.
COMMENT ON COLUMN function_permissions.action_code IS 'READ:조회|CREATE:등록|UPDATE:수정|DELETE:삭제|VERIFY:확인|AUTH:인증|APPROVE:승인|CANCEL_APPROVAL:승인취소|PRINT:출력|EXCEL:엑셀|BULK:일괄처리';
COMMENT ON COLUMN function_permissions.allowed IS '기능 권한 관리 저장 CTA에서 단일 기능 구분의 허용 여부로 갱신';
COMMENT ON COLUMN function_permissions.role_code IS 'roles.role_code 참조 의도 (FK 미선언)';

DELETE FROM function_permissions current_row
USING function_permissions duplicate_row
WHERE current_row.function_permission_id > duplicate_row.function_permission_id
  AND current_row.role_code = duplicate_row.role_code
  AND current_row.screen_id = duplicate_row.screen_id
  AND current_row.action_code = duplicate_row.action_code;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_function_permissions_role_screen_action'
    ) THEN
        ALTER TABLE function_permissions ADD CONSTRAINT uk_function_permissions_role_screen_action UNIQUE (role_code, screen_id, action_code);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS ix_function_permissions_role_screen ON function_permissions (role_code, screen_id);
CREATE INDEX IF NOT EXISTS ix_function_permissions_action ON function_permissions (action_code);
CREATE INDEX IF NOT EXISTS ix_function_permissions_allowed ON function_permissions (allowed);

INSERT INTO function_permissions (role_code, screen_id, action_code, allowed)
VALUES
    ('R09', 'SCR-FUNCTION-PERMISSION', 'READ', true),
    ('R09', 'SCR-FUNCTION-PERMISSION', 'CREATE', true),
    ('R09', 'SCR-FUNCTION-PERMISSION', 'UPDATE', true),
    ('R09', 'SCR-FUNCTION-PERMISSION', 'DELETE', false),
    ('R09', 'SCR-FUNCTION-PERMISSION', 'VERIFY', true),
    ('R09', 'SCR-FUNCTION-PERMISSION', 'AUTH', true),
    ('R09', 'SCR-FUNCTION-PERMISSION', 'APPROVE', true),
    ('R09', 'SCR-FUNCTION-PERMISSION', 'CANCEL_APPROVAL', false),
    ('R09', 'SCR-FUNCTION-PERMISSION', 'PRINT', true),
    ('R09', 'SCR-FUNCTION-PERMISSION', 'EXCEL', true),
    ('R09', 'SCR-FUNCTION-PERMISSION', 'BULK', false),
    ('R01', 'SCR-FUNCTION-PERMISSION', 'READ', false),
    ('R01', 'SCR-FUNCTION-PERMISSION', 'UPDATE', false)
ON CONFLICT (role_code, screen_id, action_code) DO UPDATE SET allowed = EXCLUDED.allowed;
