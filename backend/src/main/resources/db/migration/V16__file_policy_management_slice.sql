-- File policy management vertical slice: policy columns, constraints, indexes, menu, and seed references.
ALTER TABLE file_policies ADD COLUMN IF NOT EXISTS max_total_size_mb integer NOT NULL DEFAULT 100;
ALTER TABLE file_policies ADD COLUMN IF NOT EXISTS max_filename_length integer NOT NULL DEFAULT 120;
ALTER TABLE file_policies ADD COLUMN IF NOT EXISTS enabled boolean NOT NULL DEFAULT true;

COMMENT ON COLUMN file_policies.business_area IS '업무영역 생명주기 식별자, 생성 후 업무 의미를 변경하지 않음';
COMMENT ON COLUMN file_policies.allowed_extensions IS 'FilePolicyManagementService.saveFilePolicy 시 애플리케이션에서 갱신되는 쉼표 구분 허용 확장자';
COMMENT ON COLUMN file_policies.max_file_size_mb IS 'FilePolicyManagementService.saveFilePolicy 시 애플리케이션에서 갱신되는 단일 파일 최대용량(MB)';
COMMENT ON COLUMN file_policies.max_file_count IS 'FilePolicyManagementService.saveFilePolicy 시 애플리케이션에서 갱신되는 건당 첨부개수';
COMMENT ON COLUMN file_policies.max_total_size_mb IS 'FilePolicyManagementService.saveFilePolicy 시 애플리케이션에서 갱신되는 건당 전체 첨부용량(MB)';
COMMENT ON COLUMN file_policies.max_filename_length IS 'FilePolicyManagementService.saveFilePolicy 시 애플리케이션에서 갱신되는 파일명 최대 길이';
COMMENT ON COLUMN file_policies.malware_scan_enabled IS 'FilePolicyManagementService.saveFilePolicy 시 애플리케이션에서 갱신되는 악성파일 검사 적용 여부';
COMMENT ON COLUMN file_policies.enabled IS 'FilePolicyManagementService.saveFilePolicy 시 애플리케이션에서 갱신되는 파일정책 사용 여부';

CREATE INDEX IF NOT EXISTS ix_file_policies_business_area ON file_policies (business_area);
CREATE INDEX IF NOT EXISTS ix_file_policies_scan_enabled ON file_policies (malware_scan_enabled, enabled);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_file_policies_positive_limits'
    ) THEN
        ALTER TABLE file_policies ADD CONSTRAINT ck_file_policies_positive_limits CHECK (
            max_file_size_mb > 0
            AND max_file_count > 0
            AND max_total_size_mb >= max_file_size_mb
            AND max_filename_length BETWEEN 10 AND 255
        );
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_file_policies_allowed_extensions_format'
    ) THEN
        ALTER TABLE file_policies ADD CONSTRAINT ck_file_policies_allowed_extensions_format CHECK (allowed_extensions ~ '^[a-z0-9]+(,[a-z0-9]+)*$');
    END IF;
END $$;

INSERT INTO menus (menu_id, parent_menu_id, menu_name, screen_id, url, display_order)
VALUES ('M-FILE-POLICY', 'M-SYSTEM', '파일정책 관리', 'SCR-FILE-POLICY', '/admin/settings/file-policies', 24)
ON CONFLICT (menu_id) DO UPDATE SET
    parent_menu_id = EXCLUDED.parent_menu_id,
    menu_name = EXCLUDED.menu_name,
    screen_id = EXCLUDED.screen_id,
    url = EXCLUDED.url,
    display_order = EXCLUDED.display_order;

INSERT INTO code_details (group_id, code_value, code_name, parent_code_value, display_order)
SELECT 'BUSINESS_AREA', 'COMMON', '공통 첨부', NULL, 10
WHERE NOT EXISTS (
    SELECT 1 FROM code_details WHERE group_id = 'BUSINESS_AREA' AND code_value = 'COMMON'
);

UPDATE file_policies
SET max_total_size_mb = GREATEST(max_total_size_mb, 100),
    max_filename_length = GREATEST(max_filename_length, 120),
    enabled = true
WHERE business_area = 'COMMON';

INSERT INTO file_policies (business_area, allowed_extensions, max_file_size_mb, max_file_count, max_total_size_mb, max_filename_length, malware_scan_enabled, enabled)
SELECT 'COMMON', 'pdf,xlsx,docx,png,jpg', 20, 5, 100, 120, true, true
WHERE NOT EXISTS (SELECT 1 FROM file_policies WHERE business_area = 'COMMON');

INSERT INTO file_policies (business_area, allowed_extensions, max_file_size_mb, max_file_count, max_total_size_mb, max_filename_length, malware_scan_enabled, enabled)
SELECT 'EVALUATION', 'pdf,xlsx,docx', 30, 10, 200, 150, true, true
WHERE NOT EXISTS (SELECT 1 FROM file_policies WHERE business_area = 'EVALUATION');
