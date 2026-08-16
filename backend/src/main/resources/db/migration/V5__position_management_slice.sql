-- Position management vertical slice seed and query indexes.
COMMENT ON COLUMN position_assignments.position_code IS 'POSITION_CODE 상세코드 참조 의도 (FK 미선언)';
COMMENT ON COLUMN position_assignments.organization_code IS 'organizations.organization_code 참조 의도 (FK 미선언)';
COMMENT ON COLUMN position_assignments.valid_from IS 'KORUS Mock snapshot 또는 보직 관리 저장 시 보직 시작일로 갱신';
COMMENT ON COLUMN position_assignments.valid_to IS '보직 관리 저장 시 보직 종료일 또는 비활성 종료일로 갱신';

CREATE INDEX IF NOT EXISTS ix_position_assignments_position_code ON position_assignments (position_code);
CREATE INDEX IF NOT EXISTS ix_position_assignments_user_id ON position_assignments (user_id);
CREATE INDEX IF NOT EXISTS ix_position_assignments_organization_code ON position_assignments (organization_code);
CREATE INDEX IF NOT EXISTS ix_position_assignments_valid_period ON position_assignments (valid_from, valid_to);

INSERT INTO code_groups (group_id, group_name, description, managing_department, enabled)
VALUES ('POSITION_CODE', '보직 코드', '보직 관리 화면에서 사용하는 로컬 보직 분류', '교수지원과', true)
ON CONFLICT (group_id) DO UPDATE SET
    group_name = EXCLUDED.group_name,
    description = EXCLUDED.description,
    managing_department = EXCLUDED.managing_department,
    enabled = EXCLUDED.enabled;

INSERT INTO code_details (group_id, code_value, code_name, parent_code_value, display_order)
VALUES
    ('POSITION_CODE', 'DEPT_HEAD', '학과장', NULL, 10),
    ('POSITION_CODE', 'COLLEGE_ADMIN', '단과대학 행정담당', NULL, 20),
    ('POSITION_CODE', 'FACULTY_SUPPORT', '교수지원 담당', NULL, 30)
ON CONFLICT DO NOTHING;

INSERT INTO position_assignments (position_code, user_id, organization_code, valid_from, valid_to)
SELECT seed.position_code, seed.user_id, seed.organization_code, seed.valid_from, seed.valid_to
FROM (VALUES
    ('DEPT_HEAD', 'teacher01', 'KNUE-EDU', DATE '2026-01-01', NULL::date),
    ('COLLEGE_ADMIN', 'admin', 'KNUE-COLLEGE', DATE '2026-01-01', NULL::date),
    ('FACULTY_SUPPORT', 'admin', 'KNUE-ADMIN', DATE '2026-01-01', NULL::date)
) AS seed(position_code, user_id, organization_code, valid_from, valid_to)
WHERE NOT EXISTS (
    SELECT 1
    FROM position_assignments existing
    WHERE existing.position_code = seed.position_code
      AND existing.user_id = seed.user_id
      AND existing.organization_code = seed.organization_code
      AND existing.valid_from = seed.valid_from
);
