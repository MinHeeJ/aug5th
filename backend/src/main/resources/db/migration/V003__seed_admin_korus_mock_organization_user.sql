INSERT INTO organizations (organization_id, organization_code, organization_name, organization_type, is_used)
VALUES
('10000000-0000-0000-0000-000000000001', 'KNUE', '한국교원대학교', 'UNIVERSITY', true),
('10000000-0000-0000-0000-000000000002', 'EDU-COL', '교육과학대학', 'COLLEGE', true)
ON CONFLICT (organization_code) DO UPDATE SET
    organization_name = EXCLUDED.organization_name,
    organization_type = EXCLUDED.organization_type,
    is_used = EXCLUDED.is_used,
    updated_at = now();

INSERT INTO korus_staff_snapshot (staff_id, staff_name, organization_code, position_title, rank_title, employment_status, retirement_date, last_synced_at)
VALUES
('STAFF-ADMIN', '시스템 관리자', 'KNUE', '시스템관리자', '관리자', 'ACTIVE', NULL, now()),
('STAFF-001', '홍길동', 'EDU-COL', '학과장', '교수', 'ACTIVE', NULL, now())
ON CONFLICT (staff_id) DO UPDATE SET
    staff_name = EXCLUDED.staff_name,
    organization_code = EXCLUDED.organization_code,
    position_title = EXCLUDED.position_title,
    rank_title = EXCLUDED.rank_title,
    employment_status = EXCLUDED.employment_status,
    retirement_date = EXCLUDED.retirement_date,
    last_synced_at = EXCLUDED.last_synced_at,
    updated_at = now();

INSERT INTO users (user_id, login_id, password_hash, korus_staff_id, is_system_enabled, status)
VALUES
('20000000-0000-0000-0000-000000000001', 'admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'STAFF-ADMIN', true, 'ACTIVE'),
('20000000-0000-0000-0000-000000000002', 'faculty', '3dbd9cd5e1fb3e4f78542d53040b60990a9b4acfd6ab0b01133c5b8eeec45031', 'STAFF-001', true, 'ACTIVE')
ON CONFLICT (login_id) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    korus_staff_id = EXCLUDED.korus_staff_id,
    is_system_enabled = EXCLUDED.is_system_enabled,
    status = EXCLUDED.status,
    updated_at = now();

INSERT INTO organization_user_mappings (organization_id, user_id, position_title, valid_from, is_used)
VALUES
('10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', '시스템관리자', current_date, true),
('10000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000002', '학과장', current_date, true)
ON CONFLICT DO NOTHING;

INSERT INTO organization_relationship_history (relationship_id, organization_id, parent_organization_id, effective_start_date, change_reason, before_value, after_value, is_used)
VALUES
('30000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', current_date, '초기 시드', NULL, 'KNUE > EDU-COL', true)
ON CONFLICT (relationship_id) DO UPDATE SET
    organization_id = EXCLUDED.organization_id,
    parent_organization_id = EXCLUDED.parent_organization_id,
    effective_start_date = EXCLUDED.effective_start_date,
    change_reason = EXCLUDED.change_reason,
    after_value = EXCLUDED.after_value,
    is_used = EXCLUDED.is_used,
    updated_at = now();

INSERT INTO user_roles (user_id, role_code, assignment_type, valid_from, approved_by_user_id, after_value, change_reason, is_used)
VALUES
('20000000-0000-0000-0000-000000000001', 'R09', 'MANUAL', current_date, '20000000-0000-0000-0000-000000000001', 'R09', '초기 시드 관리자', true),
('20000000-0000-0000-0000-000000000002', 'R01', 'POSITION', current_date, '20000000-0000-0000-0000-000000000001', 'R01', '예시 교원 사용자', true)
ON CONFLICT DO NOTHING;

INSERT INTO code_groups (group_id, group_name, description, managing_department, is_used)
VALUES ('COMMON', '공통', '상세코드 관리 화면 초기 진입용 공통 그룹', '시스템관리부서', true)
ON CONFLICT (group_id) DO UPDATE SET group_name = EXCLUDED.group_name, description = EXCLUDED.description,
    managing_department = EXCLUDED.managing_department, is_used = EXCLUDED.is_used, updated_at = now();
