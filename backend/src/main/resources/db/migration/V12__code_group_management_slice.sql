-- Code group management vertical slice: common code group constraints, indexes, and seed references.
COMMENT ON COLUMN code_groups.group_id IS '코드그룹 생명주기 식별자, 생성 후 변경하지 않음';
COMMENT ON COLUMN code_groups.enabled IS 'CodeGroupManagementService.saveCodeGroup 시 애플리케이션에서 갱신되는 사용여부';
COMMENT ON COLUMN code_groups.managing_department IS 'CodeGroupManagementService.saveCodeGroup 시 애플리케이션에서 갱신되는 코드그룹 관리부서';

CREATE INDEX IF NOT EXISTS ix_code_groups_group_name ON code_groups (group_name);
CREATE INDEX IF NOT EXISTS ix_code_groups_enabled ON code_groups (enabled);
CREATE INDEX IF NOT EXISTS ix_code_groups_managing_department ON code_groups (managing_department);
CREATE INDEX IF NOT EXISTS ix_code_details_group_id ON code_details (group_id);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_code_groups_group_id_format'
    ) THEN
        ALTER TABLE code_groups ADD CONSTRAINT ck_code_groups_group_id_format CHECK (group_id ~ '^[A-Z0-9_-]+$');
    END IF;
END $$;

INSERT INTO menus (menu_id, parent_menu_id, menu_name, screen_id, url, display_order)
VALUES ('M-CODE-GROUP', 'M-SYSTEM', '코드그룹 관리', 'SCR-CODE-GROUP', '/admin/codes/groups', 19)
ON CONFLICT (menu_id) DO UPDATE SET
    parent_menu_id = EXCLUDED.parent_menu_id,
    menu_name = EXCLUDED.menu_name,
    screen_id = EXCLUDED.screen_id,
    url = EXCLUDED.url,
    display_order = EXCLUDED.display_order;

INSERT INTO code_groups (group_id, group_name, description, managing_department, enabled)
VALUES
    ('EVAL_AREA', '평가영역', '교수업적 평가영역 코드 묶음', '교수지원과', true),
    ('PROCESS_STATUS', '처리상태', '공통 승인·처리 상태 코드 묶음', '시스템관리', true),
    ('AUTH_TYPE', '인증구분', '로그인과 추가 인증 방식 코드 묶음', '정보전산원', true)
ON CONFLICT (group_id) DO UPDATE SET
    group_name = EXCLUDED.group_name,
    description = EXCLUDED.description,
    managing_department = EXCLUDED.managing_department,
    enabled = EXCLUDED.enabled;
