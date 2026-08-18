-- Menu management vertical slice: menu tree constraints, comments, indexes, and seed references.
COMMENT ON COLUMN menus.parent_menu_id IS 'menus.menu_id 참조 의도 (FK 미선언), NULL이면 대메뉴';
COMMENT ON COLUMN menus.screen_id IS '화면 라우팅 및 권한 판정 식별자, 메뉴 관리 화면에서 생명주기 식별자로 취급';
COMMENT ON COLUMN menus.display_order IS 'MenuManagementService.saveMenu 시 애플리케이션에서 갱신되는 메뉴 표시 순서';

CREATE INDEX IF NOT EXISTS ix_menus_parent_menu_id ON menus (parent_menu_id);
CREATE INDEX IF NOT EXISTS ix_menus_display_order ON menus (display_order);
CREATE INDEX IF NOT EXISTS ix_menus_url ON menus (url);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_menus_parent_not_self'
    ) THEN
        ALTER TABLE menus ADD CONSTRAINT ck_menus_parent_not_self CHECK (parent_menu_id IS NULL OR parent_menu_id <> menu_id);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_menus_screen_id'
    ) THEN
        ALTER TABLE menus ADD CONSTRAINT uk_menus_screen_id UNIQUE (screen_id);
    END IF;
END $$;

INSERT INTO menus (menu_id, parent_menu_id, menu_name, screen_id, url, display_order)
VALUES ('M-MENU-MGMT', 'M-SYSTEM', '메뉴 관리', 'SCR-MENU-MGMT', '/admin/menus', 18)
ON CONFLICT (menu_id) DO UPDATE SET
    parent_menu_id = EXCLUDED.parent_menu_id,
    menu_name = EXCLUDED.menu_name,
    screen_id = EXCLUDED.screen_id,
    url = EXCLUDED.url,
    display_order = EXCLUDED.display_order;
