-- Excel template management vertical slice: template registry versioning, validation metadata, menu, indexes, and seed references.
ALTER TABLE excel_templates ADD COLUMN IF NOT EXISTS enabled boolean NOT NULL DEFAULT true;
ALTER TABLE excel_templates ADD COLUMN IF NOT EXISTS created_by varchar(50) NOT NULL DEFAULT 'admin';
ALTER TABLE excel_templates ADD COLUMN IF NOT EXISTS created_at timestamp NOT NULL DEFAULT now();
ALTER TABLE excel_templates ADD COLUMN IF NOT EXISTS updated_by varchar(50);
ALTER TABLE excel_templates ADD COLUMN IF NOT EXISTS updated_at timestamp NOT NULL DEFAULT now();

COMMENT ON COLUMN excel_templates.template_id IS 'template_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN excel_templates.business_area IS '업무영역 코드이며 ExcelTemplateManagementService.saveExcelTemplate에서 변경하지 않는 생명주기 식별자';
COMMENT ON COLUMN excel_templates.version IS '업무영역별 업로드 양식 버전이며 ExcelTemplateManagementService.saveExcelTemplate에서 변경하지 않는 생명주기 식별자';
COMMENT ON COLUMN excel_templates.required_columns IS 'ExcelTemplateManagementService.saveExcelTemplate 시 애플리케이션에서 갱신하는 XLSX 헤더·타입·필수값·중복규칙 JSON 배열';
COMMENT ON COLUMN excel_templates.effective_date IS 'ExcelTemplateManagementService.saveExcelTemplate 시 애플리케이션에서 갱신하는 템플릿 적용 시작일';
COMMENT ON COLUMN excel_templates.download_file_id IS 'attachment_files.attachment_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN excel_templates.enabled IS 'ExcelTemplateManagementService.saveExcelTemplate 시 애플리케이션에서 갱신되는 사용 여부';
COMMENT ON COLUMN excel_templates.created_by IS 'user_accounts.user_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN excel_templates.updated_by IS 'user_accounts.user_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN excel_templates.updated_at IS 'ExcelTemplateManagementService.saveExcelTemplate 시 애플리케이션에서 갱신';

CREATE UNIQUE INDEX IF NOT EXISTS ux_excel_templates_business_area_version ON excel_templates (business_area, version);
CREATE INDEX IF NOT EXISTS ix_excel_templates_business_area ON excel_templates (business_area);
CREATE INDEX IF NOT EXISTS ix_excel_templates_enabled ON excel_templates (enabled);
CREATE INDEX IF NOT EXISTS ix_excel_templates_effective_date ON excel_templates (effective_date DESC);
CREATE INDEX IF NOT EXISTS ix_excel_templates_updated_at ON excel_templates (updated_at DESC);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_excel_templates_required_columns_array'
    ) THEN
        ALTER TABLE excel_templates ADD CONSTRAINT ck_excel_templates_required_columns_array CHECK (jsonb_typeof(required_columns) = 'array' AND jsonb_array_length(required_columns) > 0);
    END IF;
END $$;

INSERT INTO menus (menu_id, parent_menu_id, menu_name, screen_id, url, display_order)
VALUES ('M-EXCEL-TEMPLATE', 'M-FILE', '업로드 양식 관리', 'SCR-EXCEL-TEMPLATE', '/admin/excel/templates', 40)
ON CONFLICT (menu_id) DO UPDATE SET
    parent_menu_id = EXCLUDED.parent_menu_id,
    menu_name = EXCLUDED.menu_name,
    screen_id = EXCLUDED.screen_id,
    url = EXCLUDED.url,
    display_order = EXCLUDED.display_order;

INSERT INTO code_details (group_id, code_value, code_name, parent_code_value, display_order)
SELECT 'BUSINESS_AREA', 'ACHIEVEMENT', '교수업적', null, 10
WHERE NOT EXISTS (SELECT 1 FROM code_details WHERE group_id = 'BUSINESS_AREA' AND code_value = 'ACHIEVEMENT');

INSERT INTO code_details (group_id, code_value, code_name, parent_code_value, display_order)
SELECT 'BUSINESS_AREA', 'RESEARCH', '연구지원', null, 20
WHERE NOT EXISTS (SELECT 1 FROM code_details WHERE group_id = 'BUSINESS_AREA' AND code_value = 'RESEARCH');

INSERT INTO attachment_files (business_key, original_name, stored_name, extension, size_bytes, uploaded_by, uploaded_at, malware_scan_result, deleted, finalized_record, storage_present, integrity_status)
SELECT 'EXCEL_TEMPLATE:ACHIEVEMENT:2026.1', '교수업적_업로드양식_2026.xlsx', '2026/08/excel-template-achievement-2026.xlsx', 'xlsx', 51200, 'admin', TIMESTAMP '2026-08-16 12:00:00', 'CLEAN', false, false, true, 'OK'
WHERE NOT EXISTS (SELECT 1 FROM attachment_files WHERE business_key = 'EXCEL_TEMPLATE:ACHIEVEMENT:2026.1' AND original_name = '교수업적_업로드양식_2026.xlsx');

INSERT INTO excel_templates (business_area, version, required_columns, effective_date, download_file_id, enabled, created_by, created_at, updated_by, updated_at)
SELECT 'ACHIEVEMENT', '2026.1',
       '[{"name":"교번","type":"STRING","required":true,"duplicateKey":true},{"name":"업적구분","type":"STRING","required":true,"duplicateKey":false},{"name":"점수","type":"NUMBER","required":true,"duplicateKey":false}]'::jsonb,
       DATE '2026-03-01',
       af.attachment_id,
       true,
       'admin',
       TIMESTAMP '2026-08-16 12:00:00',
       'admin',
       TIMESTAMP '2026-08-16 12:00:00'
from attachment_files af
where af.business_key = 'EXCEL_TEMPLATE:ACHIEVEMENT:2026.1'
  and af.original_name = '교수업적_업로드양식_2026.xlsx'
  and not exists (select 1 from excel_templates where business_area = 'ACHIEVEMENT' and version = '2026.1');

INSERT INTO excel_templates (business_area, version, required_columns, effective_date, download_file_id, enabled, created_by, created_at, updated_by, updated_at)
SELECT 'RESEARCH', '2026.1',
       '[{"name":"교번","type":"STRING","required":true,"duplicateKey":true},{"name":"과제번호","type":"STRING","required":true,"duplicateKey":true},{"name":"연구비","type":"NUMBER","required":true,"duplicateKey":false}]'::jsonb,
       DATE '2026-03-01',
       null,
       true,
       'admin',
       TIMESTAMP '2026-08-16 12:10:00',
       'admin',
       TIMESTAMP '2026-08-16 12:10:00'
where not exists (select 1 from excel_templates where business_area = 'RESEARCH' and version = '2026.1');
