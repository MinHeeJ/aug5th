-- Code detail management vertical slice: code detail constraints, indexes, and seed references.
COMMENT ON COLUMN code_details.code_detail_id IS '상세코드 생명주기 식별자, 생성 후 변경하지 않음';
COMMENT ON COLUMN code_details.group_id IS 'code_groups.group_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN code_details.code_value IS '그룹 내 상세코드 값 생명주기 식별자, 생성 후 변경하지 않음';
COMMENT ON COLUMN code_details.code_name IS 'CodeDetailManagementService.saveCodeDetail 시 애플리케이션에서 갱신되는 상세코드명';
COMMENT ON COLUMN code_details.parent_code_value IS '같은 group_id 내 상위 code_details.code_value 참조 의도 (FK 미선언)';
COMMENT ON COLUMN code_details.display_order IS 'CodeDetailManagementService.saveCodeDetail 시 애플리케이션에서 갱신되는 표시순서이며 0 이하는 비활성 표시로 해석';

CREATE INDEX IF NOT EXISTS ix_code_details_group_value ON code_details (group_id, code_value);
CREATE INDEX IF NOT EXISTS ix_code_details_display_order ON code_details (display_order);
CREATE INDEX IF NOT EXISTS ix_code_details_code_name ON code_details (code_name);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_code_details_group_value'
    ) THEN
        ALTER TABLE code_details ADD CONSTRAINT uq_code_details_group_value UNIQUE (group_id, code_value);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_code_details_code_value_format'
    ) THEN
        ALTER TABLE code_details ADD CONSTRAINT ck_code_details_code_value_format CHECK (code_value ~ '^[A-Z0-9_-]+$');
    END IF;
END $$;

INSERT INTO menus (menu_id, parent_menu_id, menu_name, screen_id, url, display_order)
VALUES ('M-CODE-DETAIL', 'M-SYSTEM', '상세코드 관리', 'SCR-CODE-DETAIL', '/admin/codes/details', 20)
ON CONFLICT (menu_id) DO UPDATE SET
    parent_menu_id = EXCLUDED.parent_menu_id,
    menu_name = EXCLUDED.menu_name,
    screen_id = EXCLUDED.screen_id,
    url = EXCLUDED.url,
    display_order = EXCLUDED.display_order;

INSERT INTO code_details (group_id, code_value, code_name, parent_code_value, display_order)
VALUES
    ('EVAL_AREA', 'TEACHING', '교육영역', null, 10),
    ('EVAL_AREA', 'RESEARCH', '연구영역', null, 20),
    ('EVAL_AREA', 'SERVICE', '봉사영역', null, 30),
    ('PROCESS_STATUS', 'DRAFT', '작성중', null, 10),
    ('PROCESS_STATUS', 'SUBMITTED', '제출', null, 20),
    ('PROCESS_STATUS', 'APPROVED', '승인', null, 30),
    ('AUTH_TYPE', 'PASSWORD', '비밀번호', null, 10),
    ('AUTH_TYPE', 'SSO', 'SSO', null, 20)
ON CONFLICT (group_id, code_value) DO UPDATE SET
    code_name = EXCLUDED.code_name,
    parent_code_value = EXCLUDED.parent_code_value,
    display_order = EXCLUDED.display_order;
