-- Organization management vertical slice hierarchy seed and query indexes.
COMMENT ON COLUMN organizations.parent_organization_code IS 'organizations.organization_code 참조 의도 (FK 미선언)';
COMMENT ON COLUMN organizations.valid_from IS 'KORUS Mock snapshot 또는 조직 관리 저장 시 적용 시작일로 갱신';
COMMENT ON COLUMN organizations.valid_to IS '조직 관리 저장 시 개편 종료일 또는 비활성 종료일로 갱신';

CREATE INDEX IF NOT EXISTS ix_organizations_parent_code ON organizations (parent_organization_code);
CREATE INDEX IF NOT EXISTS ix_organizations_valid_period ON organizations (valid_from, valid_to);
CREATE INDEX IF NOT EXISTS ix_organizations_name ON organizations (organization_name);

INSERT INTO organizations (organization_code, organization_name, parent_organization_code, valid_from, valid_to, enabled)
VALUES
    ('KNUE-ROOT', '한국교원대학교', NULL, DATE '2026-01-01', NULL, true),
    ('KNUE-COLLEGE', '사범대학', 'KNUE-ROOT', DATE '2026-01-01', NULL, true),
    ('KNUE-GRAD', '교육대학원', 'KNUE-ROOT', DATE '2026-01-01', NULL, true),
    ('KNUE-ADMIN', '교수지원과', 'KNUE-ROOT', DATE '2026-01-01', NULL, true)
ON CONFLICT (organization_code) DO UPDATE SET
    organization_name = EXCLUDED.organization_name,
    parent_organization_code = EXCLUDED.parent_organization_code,
    valid_from = EXCLUDED.valid_from,
    valid_to = EXCLUDED.valid_to,
    enabled = EXCLUDED.enabled;

UPDATE organizations
SET parent_organization_code = 'KNUE-COLLEGE'
WHERE organization_code IN ('KNUE-EDU', 'KNUE-COM')
  AND parent_organization_code IS NULL;
