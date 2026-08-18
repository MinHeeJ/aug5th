-- User-role management vertical slice: effective period query fields and seed data.
ALTER TABLE user_roles ADD COLUMN IF NOT EXISTS assignment_source varchar(20) NOT NULL DEFAULT 'MANUAL';
COMMENT ON COLUMN user_roles.user_role_id IS '사용자 역할 관리 화면의 생명주기 식별자이며 저장 CTA에서 변경하지 않는다';
COMMENT ON COLUMN user_roles.valid_from IS '사용자 역할 유효기간 시작일이며 현재 적용 역할 판정에 사용';
COMMENT ON COLUMN user_roles.valid_to IS '사용자 역할 관리 저장 시 회수 또는 재부여 유효기간 종료일로 갱신';
COMMENT ON COLUMN user_roles.assignment_source IS 'MANUAL:수동부여|POSITION:보직기반';

CREATE INDEX IF NOT EXISTS ix_user_roles_valid_period ON user_roles (valid_from, valid_to);
CREATE INDEX IF NOT EXISTS ix_user_roles_assignment_source ON user_roles (assignment_source);
CREATE INDEX IF NOT EXISTS ix_user_roles_role_user ON user_roles (role_code, user_id);

UPDATE user_roles
SET assignment_source = 'MANUAL'
WHERE assignment_source IS NULL;

INSERT INTO user_roles (user_id, role_code, valid_from, valid_to, approver_id, assignment_source)
VALUES
    ('admin', 'R09', DATE '2026-01-01', NULL, 'admin', 'MANUAL'),
    ('teacher01', 'R01', DATE '2026-01-01', NULL, 'admin', 'MANUAL')
ON CONFLICT DO NOTHING;
