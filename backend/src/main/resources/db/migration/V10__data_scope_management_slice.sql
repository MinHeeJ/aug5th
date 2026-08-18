-- Data-scope permission management vertical slice: indexes, comments, and seed references.
COMMENT ON COLUMN data_scope_permissions.role_code IS 'roles.role_code 참조 의도 (FK 미선언)';
COMMENT ON COLUMN data_scope_permissions.scope_type IS 'SELF:본인|DEPARTMENT:소속학과|COLLEGE:단과대학|BUSINESS:담당업무|ALL:전체';
COMMENT ON COLUMN data_scope_permissions.organization_code IS 'organizations.organization_code 참조 의도 (FK 미선언), DEPARTMENT/COLLEGE 범위 서버 조회조건 적용 시 사용';
COMMENT ON COLUMN data_scope_permissions.business_area IS '담당업무 데이터 범위 서버 조회조건 적용 시 사용하는 업무영역 코드';

DELETE FROM data_scope_permissions current_row
USING data_scope_permissions duplicate_row
WHERE current_row.data_scope_id > duplicate_row.data_scope_id
  AND current_row.role_code = duplicate_row.role_code;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_data_scope_permissions_role'
    ) THEN
        ALTER TABLE data_scope_permissions ADD CONSTRAINT uk_data_scope_permissions_role UNIQUE (role_code);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS ix_data_scope_permissions_scope_type ON data_scope_permissions (scope_type);
CREATE INDEX IF NOT EXISTS ix_data_scope_permissions_organization ON data_scope_permissions (organization_code);
CREATE INDEX IF NOT EXISTS ix_data_scope_permissions_business_area ON data_scope_permissions (business_area);

INSERT INTO data_scope_permissions (role_code, scope_type, organization_code, business_area)
VALUES
    ('R09', 'ALL', 'KNUE', 'COMMON_FOUNDATION'),
    ('R08', 'ALL', 'KNUE', 'COMMON_FOUNDATION'),
    ('R07', 'BUSINESS', NULL, 'COMMON_FOUNDATION'),
    ('R04', 'ALL', 'KNUE', 'COMMON_FOUNDATION'),
    ('R03', 'COLLEGE', 'KNUE-COLLEGE', NULL),
    ('R02', 'DEPARTMENT', 'KNUE-EDU', NULL),
    ('R01', 'SELF', NULL, NULL)
ON CONFLICT (role_code) DO UPDATE SET
    scope_type = EXCLUDED.scope_type,
    organization_code = EXCLUDED.organization_code,
    business_area = EXCLUDED.business_area;
