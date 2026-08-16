-- Excel download vertical slice: request persistence, generated xlsx file metadata, menu, indexes, and seed references.
ALTER TABLE excel_download_requests ALTER COLUMN created_at SET DEFAULT now();

COMMENT ON COLUMN excel_download_requests.download_id IS 'excel_download_requests.download_id 생명주기 식별자';
COMMENT ON COLUMN excel_download_requests.requester_id IS 'user_accounts.user_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN excel_download_requests.query_condition IS 'ExcelDownloadManagementService.createExcelDownload 시 현재 조회조건과 업무영역을 JSON으로 기록';
COMMENT ON COLUMN excel_download_requests.data_scope_applied IS 'ExcelDownloadManagementService.createExcelDownload 시 사용자 역할과 데이터범위 권한을 JSON으로 기록';
COMMENT ON COLUMN excel_download_requests.file_id IS 'attachment_files.attachment_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN excel_download_requests.created_at IS 'ExcelDownloadManagementService.createExcelDownload 시 애플리케이션 요청 처리 시점에 DB now()로 기록';

CREATE INDEX IF NOT EXISTS ix_excel_download_requests_requester_id ON excel_download_requests (requester_id);
CREATE INDEX IF NOT EXISTS ix_excel_download_requests_file_id ON excel_download_requests (file_id);
CREATE INDEX IF NOT EXISTS ix_excel_download_requests_created_at ON excel_download_requests (created_at DESC);
CREATE INDEX IF NOT EXISTS ix_excel_download_requests_query_condition_gin ON excel_download_requests USING gin (query_condition);
CREATE INDEX IF NOT EXISTS ix_excel_download_requests_data_scope_gin ON excel_download_requests USING gin (data_scope_applied);

INSERT INTO menus (menu_id, parent_menu_id, menu_name, screen_id, url, display_order)
VALUES ('M-EXCEL-DOWNLOAD', 'M-FILE', '엑셀 다운로드', 'SCR-EXCEL-DOWNLOAD', '/admin/excel/downloads', 60)
ON CONFLICT (menu_id) DO UPDATE SET
    parent_menu_id = EXCLUDED.parent_menu_id,
    menu_name = EXCLUDED.menu_name,
    screen_id = EXCLUDED.screen_id,
    url = EXCLUDED.url,
    display_order = EXCLUDED.display_order;

INSERT INTO attachment_files (
    business_key, original_name, stored_name, extension, size_bytes,
    uploaded_by, uploaded_at, malware_scan_result, deleted, finalized_record,
    storage_present, integrity_status
)
SELECT 'EXCEL_DOWNLOAD:ACHIEVEMENT', '교수업적_조회결과_2026.xlsx', 'excel-downloads/admin/교수업적_조회결과_2026.xlsx', 'xlsx',
       2048, 'admin', TIMESTAMP '2026-08-16 14:00:00', 'CLEAN', false, false, true, 'OK'
WHERE NOT EXISTS (
    SELECT 1 FROM attachment_files WHERE business_key = 'EXCEL_DOWNLOAD:ACHIEVEMENT' AND original_name = '교수업적_조회결과_2026.xlsx'
);

INSERT INTO excel_download_requests (requester_id, query_condition, data_scope_applied, file_id, created_at)
SELECT 'admin',
       '{"businessArea":"ACHIEVEMENT","q":"성과","year":"2026"}'::jsonb,
       '{"role":"R09","scope":"ALL","serverEnforced":true}'::jsonb,
       af.attachment_id,
       TIMESTAMP '2026-08-16 14:00:00'
FROM attachment_files af
WHERE af.business_key = 'EXCEL_DOWNLOAD:ACHIEVEMENT'
  AND af.original_name = '교수업적_조회결과_2026.xlsx'
  AND NOT EXISTS (
      SELECT 1 FROM excel_download_requests edr
      WHERE edr.requester_id = 'admin'
        AND edr.file_id = af.attachment_id
        AND edr.query_condition @> '{"businessArea":"ACHIEVEMENT","year":"2026"}'::jsonb
  );
