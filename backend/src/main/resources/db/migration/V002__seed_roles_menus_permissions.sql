INSERT INTO roles (role_code, role_name, role_purpose, assignment_criteria, default_data_scope, is_used)
VALUES
('R01', '교원', '교원 사용자 기본 역할', 'KORUS 교원 보직/직급 기준', '본인 데이터', true),
('R02', '학과장', '학과 단위 관리 역할', '학과장 보직 기준', '소속 학과', true),
('R03', '단과대학(원) 행정실', '단과대학 또는 대학원 행정 처리 역할', '행정실 보직 기준', '소속 단과대학/대학원', true),
('R04', '교수지원과', '교수지원과 업무 관리 역할', '교수지원과 부서 기준', '교수지원과 관할', true),
('R05', '산학협력단', '산학협력단 업무 관리 역할', '산학협력단 부서 기준', '산학협력단 관할', true),
('R06', '입학인재관리과', '입학인재관리과 업무 관리 역할', '입학인재관리과 부서 기준', '입학인재관리과 관할', true),
('R07', '실적부서', '실적부서 업무 관리 역할', '실적 담당 부서 기준', '담당 실적부서', true),
('R08', '점수산출 감사자', '점수산출 결과 검토 역할', '감사자 지정 기준', '감사 대상 범위', true),
('R09', '시스템관리자', '시스템 관리 전권 역할', '시드 관리자 및 시스템관리자 지정', '전체', true)
ON CONFLICT (role_code) DO UPDATE SET
    role_name = EXCLUDED.role_name,
    role_purpose = EXCLUDED.role_purpose,
    assignment_criteria = EXCLUDED.assignment_criteria,
    default_data_scope = EXCLUDED.default_data_scope,
    is_used = EXCLUDED.is_used,
    updated_at = now();

WITH main_menu AS (
    INSERT INTO menus (menu_id, parent_menu_id, menu_level, display_order, menu_name, screen_id, url, icon, business_division, description, is_used)
    VALUES ('00000000-0000-0000-0000-000000000100', NULL, 'MAIN', 1, '시스템 관리', NULL, NULL, 'settings', '공통', '공통기능 1차 시스템 관리 대메뉴', true)
    ON CONFLICT (menu_id) DO UPDATE SET menu_name = EXCLUDED.menu_name, display_order = EXCLUDED.display_order, updated_at = now()
    RETURNING menu_id
), middle_seed AS (
    INSERT INTO menus (menu_id, parent_menu_id, menu_level, display_order, menu_name, screen_id, url, icon, business_division, description, is_used)
    VALUES
    ('00000000-0000-0000-0000-000000000110', '00000000-0000-0000-0000-000000000100', 'MIDDLE', 1, '사용자·조직', NULL, NULL, 'users', '공통', '사용자와 조직 기준정보', true),
    ('00000000-0000-0000-0000-000000000120', '00000000-0000-0000-0000-000000000100', 'MIDDLE', 2, '역할·권한', NULL, NULL, 'shield', '공통', '역할 및 권한 관리', true),
    ('00000000-0000-0000-0000-000000000130', '00000000-0000-0000-0000-000000000100', 'MIDDLE', 3, '메뉴', NULL, NULL, 'menu', '공통', '메뉴 구조와 정보', true),
    ('00000000-0000-0000-0000-000000000140', '00000000-0000-0000-0000-000000000100', 'MIDDLE', 4, '공통코드', NULL, NULL, 'code', '공통', '공통코드 관리', true)
    ON CONFLICT (menu_id) DO UPDATE SET parent_menu_id = EXCLUDED.parent_menu_id, display_order = EXCLUDED.display_order, menu_name = EXCLUDED.menu_name, updated_at = now()
    RETURNING menu_id
), sub_seed AS (
    INSERT INTO menus (menu_id, parent_menu_id, menu_level, display_order, menu_name, screen_id, url, icon, business_division, description, is_used)
    VALUES
    ('00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000110', 'SUB', 1, '사용자 관리', 'SCR-USER-MGMT', '/api/admin/users', 'user', '공통', '사용자 조건 조회 및 시스템 사용여부 관리', true),
    ('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000110', 'SUB', 2, '조직 관리', 'SCR-ORG-MGMT', '/api/admin/organizations', 'building', '공통', '조직 기준정보와 계층 관리', true),
    ('00000000-0000-0000-0000-000000000203', '00000000-0000-0000-0000-000000000120', 'SUB', 1, '역할 관리', 'SCR-ROLE-MGMT', '/api/admin/roles', 'badge', '공통', 'R01~R09 역할 기준정보 관리', true),
    ('00000000-0000-0000-0000-000000000204', '00000000-0000-0000-0000-000000000120', 'SUB', 2, '사용자 역할 관리', 'SCR-USER-ROLE', '/api/admin/user-roles', 'id-card', '공통', '사용자별 역할 부여와 회수 관리', true),
    ('00000000-0000-0000-0000-000000000205', '00000000-0000-0000-0000-000000000120', 'SUB', 3, '메뉴 권한 관리', 'SCR-MENU-PERMISSION', '/api/admin/menu-permissions', 'lock', '공통', '대상별 메뉴 접근권한 관리', true),
    ('00000000-0000-0000-0000-000000000206', '00000000-0000-0000-0000-000000000130', 'SUB', 1, '메뉴 구조 관리', 'SCR-MENU-STRUCTURE', '/api/admin/menu-structure', 'tree', '공통', '메뉴 계층과 표시순서 관리', true),
    ('00000000-0000-0000-0000-000000000207', '00000000-0000-0000-0000-000000000130', 'SUB', 2, '메뉴 정보 관리', 'SCR-MENU-INFO', '/api/admin/menus', 'layout', '공통', '메뉴 실행정보 관리', true),
    ('00000000-0000-0000-0000-000000000208', '00000000-0000-0000-0000-000000000140', 'SUB', 1, '코드그룹 관리', 'SCR-CODE-GROUP', '/api/admin/code-groups', 'folder-code', '공통', '코드그룹 기준정보 관리', true),
    ('00000000-0000-0000-0000-000000000209', '00000000-0000-0000-0000-000000000140', 'SUB', 2, '상세코드 관리', 'SCR-CODE-DETAIL', '/api/admin/code-groups/COMMON/codes', 'list-code', '공통', '상세코드 기준정보 관리', true)
    ON CONFLICT (menu_id) DO UPDATE SET parent_menu_id = EXCLUDED.parent_menu_id, display_order = EXCLUDED.display_order, menu_name = EXCLUDED.menu_name,
        screen_id = EXCLUDED.screen_id, url = EXCLUDED.url, icon = EXCLUDED.icon, business_division = EXCLUDED.business_division,
        description = EXCLUDED.description, is_used = EXCLUDED.is_used, updated_at = now()
    RETURNING menu_id
)
INSERT INTO menu_permissions (target_type, target_id, menu_id, is_allowed, is_used)
SELECT 'ROLE', 'R09', s.menu_id, true, true
FROM sub_seed s
ON CONFLICT (target_type, target_id, menu_id) DO UPDATE SET is_allowed = true, is_used = true, updated_at = now();
