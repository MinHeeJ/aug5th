-- Base year management vertical slice: constraints, indexes, menu, and seed references.
COMMENT ON COLUMN base_years.base_year IS '기준연도 생명주기 식별자, 생성 후 변경하지 않음';
COMMENT ON COLUMN base_years.default_query_year IS 'BaseYearManagementService.saveBaseYear 시 애플리케이션에서 갱신되는 사용자 화면 기본 조회연도';
COMMENT ON COLUMN base_years.copy_baseline_enabled IS 'BaseYearManagementService.saveBaseYear 시 애플리케이션에서 갱신되는 연도별 기준정보 복사 허용 여부';
COMMENT ON COLUMN base_years.reset_enabled IS 'BaseYearManagementService.saveBaseYear 시 애플리케이션에서 갱신되는 연도별 기준정보 초기화 허용 여부';
COMMENT ON COLUMN base_years.enabled IS 'BaseYearManagementService.saveBaseYear 시 애플리케이션에서 갱신되는 기준연도 사용 여부';

CREATE INDEX IF NOT EXISTS ix_base_years_default_query_year ON base_years (default_query_year);
CREATE INDEX IF NOT EXISTS ix_base_years_enabled_year ON base_years (enabled, base_year);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_base_years_year_format'
    ) THEN
        ALTER TABLE base_years ADD CONSTRAINT ck_base_years_year_format CHECK (base_year ~ '^[0-9]{4}$' AND default_query_year ~ '^[0-9]{4}$');
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_base_years_default_query_period'
    ) THEN
        ALTER TABLE base_years ADD CONSTRAINT ck_base_years_default_query_period CHECK (default_query_year::integer <= base_year::integer AND base_year::integer - default_query_year::integer <= 1);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_base_years_reset_requires_copy'
    ) THEN
        ALTER TABLE base_years ADD CONSTRAINT ck_base_years_reset_requires_copy CHECK (copy_baseline_enabled OR NOT reset_enabled);
    END IF;
END $$;

INSERT INTO menus (menu_id, parent_menu_id, menu_name, screen_id, url, display_order)
VALUES ('M-BASE-YEAR', 'M-SYSTEM', '기준연도 관리', 'SCR-BASE-YEAR', '/admin/settings/base-years', 23)
ON CONFLICT (menu_id) DO UPDATE SET
    parent_menu_id = EXCLUDED.parent_menu_id,
    menu_name = EXCLUDED.menu_name,
    screen_id = EXCLUDED.screen_id,
    url = EXCLUDED.url,
    display_order = EXCLUDED.display_order;

INSERT INTO base_years (base_year, default_query_year, copy_baseline_enabled, reset_enabled, enabled)
VALUES
    ('2025', '2025', false, false, true),
    ('2026', '2026', true, false, true),
    ('2027', '2026', true, false, false)
ON CONFLICT (base_year) DO UPDATE SET
    default_query_year = EXCLUDED.default_query_year,
    copy_baseline_enabled = EXCLUDED.copy_baseline_enabled,
    reset_enabled = EXCLUDED.reset_enabled,
    enabled = EXCLUDED.enabled;
