-- User management vertical slice seed and KORUS snapshot display metadata.
ALTER TABLE korus_personnel_snapshots ADD COLUMN IF NOT EXISTS name_masked varchar(100);
ALTER TABLE korus_personnel_snapshots ADD COLUMN IF NOT EXISTS retirement_date date;
ALTER TABLE korus_personnel_snapshots ADD COLUMN IF NOT EXISTS last_synced_at timestamp;

COMMENT ON COLUMN korus_personnel_snapshots.name_masked IS '개인정보 마스킹 서비스 또는 Mock snapshot 생성 시 갱신되는 표시용 성명';
COMMENT ON COLUMN korus_personnel_snapshots.retirement_date IS 'KORUS Mock snapshot 동기화 시 갱신되는 퇴직일자';
COMMENT ON COLUMN korus_personnel_snapshots.last_synced_at IS 'KORUS Mock snapshot 동기화 시 갱신되는 최종 동기화일시';

CREATE INDEX IF NOT EXISTS ix_korus_personnel_employee_no ON korus_personnel_snapshots (employee_no);
CREATE INDEX IF NOT EXISTS ix_korus_personnel_department_code ON korus_personnel_snapshots (department_code);
CREATE INDEX IF NOT EXISTS ix_korus_personnel_employment_status ON korus_personnel_snapshots (employment_status);

INSERT INTO organizations (organization_code, organization_name, parent_organization_code, valid_from, valid_to, enabled)
VALUES
    ('KNUE-EDU', '교육학과', NULL, DATE '2026-01-01', NULL, true),
    ('KNUE-COM', '컴퓨터교육과', NULL, DATE '2026-01-01', NULL, true)
ON CONFLICT (organization_code) DO UPDATE SET
    organization_name = EXCLUDED.organization_name,
    parent_organization_code = EXCLUDED.parent_organization_code,
    valid_from = EXCLUDED.valid_from,
    valid_to = EXCLUDED.valid_to,
    enabled = EXCLUDED.enabled;

INSERT INTO user_accounts (user_id, enabled, role_summary, created_at, updated_at, status, password_hash)
VALUES
    ('teacher01', true, 'R01 교원', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACTIVE', NULL),
    ('teacher02', false, 'R01 교원', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'INACTIVE', NULL)
ON CONFLICT (user_id) DO UPDATE SET
    enabled = EXCLUDED.enabled,
    role_summary = EXCLUDED.role_summary,
    status = EXCLUDED.status,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO korus_personnel_snapshots (person_id, employee_no, name_encrypted, name_masked, department_code, rank_name, employment_status, retirement_date, last_synced_at)
VALUES
    ('admin', 'ADM-0001', NULL, '관리자', 'KNUE-COM', '시스템관리자', 'ACTIVE', NULL, CURRENT_TIMESTAMP),
    ('teacher01', 'P-2026-001', NULL, '김교*', 'KNUE-EDU', '교수', 'ACTIVE', NULL, CURRENT_TIMESTAMP),
    ('teacher02', 'P-2026-002', NULL, '박교*', 'KNUE-COM', '부교수', 'RETIRED', DATE '2026-02-28', CURRENT_TIMESTAMP)
ON CONFLICT (person_id) DO UPDATE SET
    employee_no = EXCLUDED.employee_no,
    name_encrypted = EXCLUDED.name_encrypted,
    name_masked = EXCLUDED.name_masked,
    department_code = EXCLUDED.department_code,
    rank_name = EXCLUDED.rank_name,
    employment_status = EXCLUDED.employment_status,
    retirement_date = EXCLUDED.retirement_date,
    last_synced_at = EXCLUDED.last_synced_at;

INSERT INTO user_roles (user_id, role_code, valid_from, valid_to, approver_id)
VALUES
    ('teacher01', 'R01', CURRENT_DATE, NULL, 'admin'),
    ('teacher02', 'R01', CURRENT_DATE, NULL, 'admin')
ON CONFLICT DO NOTHING;

INSERT INTO position_assignments (position_code, user_id, organization_code, valid_from, valid_to)
VALUES
    ('PROFESSOR', 'teacher01', 'KNUE-EDU', CURRENT_DATE, NULL),
    ('FORMER_PROFESSOR', 'teacher02', 'KNUE-COM', DATE '2026-01-01', DATE '2026-02-28')
ON CONFLICT DO NOTHING;
