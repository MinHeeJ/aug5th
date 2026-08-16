-- System configuration management vertical slice: constraints, indexes, and seed references.
COMMENT ON COLUMN system_configurations.config_key IS '공통 환경설정 생명주기 식별자, 생성 후 변경하지 않음';
COMMENT ON COLUMN system_configurations.config_value IS 'SystemConfigurationManagementService.saveSystemConfiguration 시 애플리케이션에서 갱신되는 전역 설정값';
COMMENT ON COLUMN system_configurations.unit IS '설정값 단위이며 사용자·업무별 개별 단위를 허용하지 않음';
COMMENT ON COLUMN system_configurations.value_range IS 'SystemConfigurationManagementService.saveSystemConfiguration 시 서버 검증에 사용하는 최소-최대 숫자 범위';
COMMENT ON COLUMN system_configurations.enabled IS 'SystemConfigurationManagementService.saveSystemConfiguration 시 애플리케이션에서 갱신되는 전역 사용 여부';

CREATE INDEX IF NOT EXISTS ix_system_configurations_key_enabled ON system_configurations (config_key, enabled);
CREATE INDEX IF NOT EXISTS ix_system_configurations_unit ON system_configurations (unit);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_system_configurations_config_key_format'
    ) THEN
        ALTER TABLE system_configurations ADD CONSTRAINT ck_system_configurations_config_key_format CHECK (config_key ~ '^[A-Z0-9_-]+$');
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_system_configurations_config_value_not_blank'
    ) THEN
        ALTER TABLE system_configurations ADD CONSTRAINT ck_system_configurations_config_value_not_blank CHECK (length(trim(config_value)) > 0);
    END IF;
END $$;

INSERT INTO menus (menu_id, parent_menu_id, menu_name, screen_id, url, display_order)
VALUES ('M-SYSTEM-CONFIG', 'M-SYSTEM', '공통 환경설정', 'SCR-SYSTEM-CONFIG', '/admin/settings/common', 22)
ON CONFLICT (menu_id) DO UPDATE SET
    parent_menu_id = EXCLUDED.parent_menu_id,
    menu_name = EXCLUDED.menu_name,
    screen_id = EXCLUDED.screen_id,
    url = EXCLUDED.url,
    display_order = EXCLUDED.display_order;

INSERT INTO system_configurations (config_key, config_value, unit, value_range, enabled)
VALUES
    ('SESSION_IDLE_MINUTES', '30', '분', '5-240', true),
    ('DEFAULT_PAGE_SIZE', '20', '건', '20-100', true),
    ('DEFAULT_SEARCH_PERIOD_DAYS', '30', '일', '1-366', true),
    ('BULK_QUERY_THRESHOLD', '1000', '건', '100-10000', true),
    ('LONG_TASK_NOTICE_SECONDS', '10', '초', '1-600', true)
ON CONFLICT (config_key) DO UPDATE SET
    config_value = EXCLUDED.config_value,
    unit = EXCLUDED.unit,
    value_range = EXCLUDED.value_range,
    enabled = EXCLUDED.enabled;
