-- Audit log management vertical slice: immutable audit-log search, confirmation trail, menu, indexes, and seed references.
COMMENT ON COLUMN audit_logs.audit_log_id IS 'audit_logs.audit_log_id 생명주기 식별자';
COMMENT ON COLUMN audit_logs.target_key IS '감사 대상 업무키 또는 리소스 식별자 (FK 미선언)';
COMMENT ON COLUMN audit_logs.actor_id IS 'user_accounts.user_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN audit_logs.before_value IS '업무 서비스의 등록·수정·삭제·조회 확인 처리 직전 원문 JSON으로 애플리케이션에서 기록';
COMMENT ON COLUMN audit_logs.after_value IS '업무 서비스의 등록·수정·삭제·조회 확인 처리 직후 원문 JSON으로 애플리케이션에서 기록';
COMMENT ON COLUMN audit_logs.log_type IS 'LOGIN:로그인|LOGOUT:로그아웃|CREATE:등록|UPDATE:수정|DELETE:삭제|READ:조회|AUTHORIZATION:권한';
COMMENT ON COLUMN audit_logs.result IS 'SUCCESS:성공|DENIED:거부|FAILED:실패';

CREATE INDEX IF NOT EXISTS ix_audit_logs_log_type ON audit_logs (log_type);
CREATE INDEX IF NOT EXISTS ix_audit_logs_result ON audit_logs (result);
CREATE INDEX IF NOT EXISTS ix_audit_logs_actor_id ON audit_logs (actor_id);
CREATE INDEX IF NOT EXISTS ix_audit_logs_target_key ON audit_logs (target_key);
CREATE INDEX IF NOT EXISTS ix_audit_logs_recent ON audit_logs (audit_log_id DESC);

INSERT INTO menus (menu_id, parent_menu_id, menu_name, screen_id, url, display_order)
VALUES ('M-AUDIT-LOG', 'M-SECURITY', '감사 로그 관리', 'SCR-AUDIT-LOG', '/admin/security/audit-logs', 90)
ON CONFLICT (menu_id) DO UPDATE SET
    parent_menu_id = EXCLUDED.parent_menu_id,
    menu_name = EXCLUDED.menu_name,
    screen_id = EXCLUDED.screen_id,
    url = EXCLUDED.url,
    display_order = EXCLUDED.display_order;

INSERT INTO menu_permissions (target_type, target_id, menu_id, allowed)
SELECT 'ROLE', role_code, 'M-AUDIT-LOG', role_code = 'R09'
FROM roles
WHERE NOT EXISTS (
    SELECT 1 FROM menu_permissions mp
    WHERE mp.target_type = 'ROLE'
      AND mp.target_id = roles.role_code
      AND mp.menu_id = 'M-AUDIT-LOG'
);

INSERT INTO audit_logs (log_type, target_key, actor_id, before_value, after_value, result)
SELECT 'AUTHORIZATION', 'roles:R09', 'admin', '{"roleCode":"R09","allowed":false}'::jsonb, '{"roleCode":"R09","allowed":true}'::jsonb, 'SUCCESS'
WHERE NOT EXISTS (
    SELECT 1 FROM audit_logs
    WHERE log_type = 'AUTHORIZATION'
      AND target_key = 'roles:R09'
      AND actor_id = 'admin'
);

INSERT INTO audit_logs (log_type, target_key, actor_id, before_value, after_value, result)
SELECT 'READ', 'privacy:masked-field', 'admin', '{"fieldName":"residentRegistrationNo"}'::jsonb, '{"masked":true,"reason":"중요정보 조회 로그"}'::jsonb, 'SUCCESS'
WHERE NOT EXISTS (
    SELECT 1 FROM audit_logs
    WHERE log_type = 'READ'
      AND target_key = 'privacy:masked-field'
      AND actor_id = 'admin'
);
