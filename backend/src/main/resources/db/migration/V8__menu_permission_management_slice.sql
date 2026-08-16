-- Menu-permission management vertical slice: indexes, comments, and seed references.
COMMENT ON COLUMN menu_permissions.target_type IS 'ROLE:역할|ORG:조직|USER:사용자';
COMMENT ON COLUMN menu_permissions.allowed IS '메뉴 권한 관리 저장 CTA에서 접근 허용 여부로 갱신';

DELETE FROM menu_permissions current_row
USING menu_permissions duplicate_row
WHERE current_row.menu_permission_id > duplicate_row.menu_permission_id
  AND current_row.target_type = duplicate_row.target_type
  AND current_row.target_id = duplicate_row.target_id
  AND current_row.menu_id = duplicate_row.menu_id;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_menu_permissions_target_menu'
    ) THEN
        ALTER TABLE menu_permissions ADD CONSTRAINT uk_menu_permissions_target_menu UNIQUE (target_type, target_id, menu_id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS ix_menu_permissions_target ON menu_permissions (target_type, target_id);
CREATE INDEX IF NOT EXISTS ix_menu_permissions_menu ON menu_permissions (menu_id);
CREATE INDEX IF NOT EXISTS ix_menu_permissions_allowed ON menu_permissions (allowed);

INSERT INTO menu_permissions (target_type, target_id, menu_id, allowed)
VALUES
    ('ROLE', 'R09', 'M-MENU-PERMISSION', true),
    ('ROLE', 'R09', 'M-USER-ROLE-MGMT', true),
    ('ROLE', 'R01', 'M-MENU-PERMISSION', false)
ON CONFLICT (target_type, target_id, menu_id) DO UPDATE SET allowed = EXCLUDED.allowed;
