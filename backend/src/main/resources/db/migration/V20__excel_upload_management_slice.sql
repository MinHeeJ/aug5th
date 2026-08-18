-- Excel upload vertical slice: upload history, row-level validation errors, menu, indexes, and seed references.
ALTER TABLE excel_upload_histories ADD COLUMN IF NOT EXISTS excluded_count integer NOT NULL DEFAULT 0;
ALTER TABLE excel_upload_histories ADD COLUMN IF NOT EXISTS saved_count integer NOT NULL DEFAULT 0;
ALTER TABLE excel_upload_histories ADD COLUMN IF NOT EXISTS upload_status varchar(30) NOT NULL DEFAULT 'SUCCESS';
ALTER TABLE excel_upload_histories ADD COLUMN IF NOT EXISTS uploaded_at timestamp NOT NULL DEFAULT now();

COMMENT ON COLUMN excel_upload_histories.upload_id IS 'upload_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN excel_upload_histories.template_id IS 'excel_templates.template_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN excel_upload_histories.uploader_id IS 'user_accounts.user_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN excel_upload_histories.file_name IS '업로드 요청 원본 XLSX 파일명';
COMMENT ON COLUMN excel_upload_histories.total_count IS 'ExcelUploadManagementService.uploadExcel 시 애플리케이션에서 산출한 전체 행 수';
COMMENT ON COLUMN excel_upload_histories.success_count IS 'ExcelUploadManagementService.uploadExcel 시 애플리케이션에서 산출한 정상 행 수';
COMMENT ON COLUMN excel_upload_histories.error_count IS 'ExcelUploadManagementService.uploadExcel 시 애플리케이션에서 산출한 오류 행 수';
COMMENT ON COLUMN excel_upload_histories.processing_time_ms IS 'ExcelUploadManagementService.uploadExcel 시 애플리케이션에서 산출한 처리 소요시간(ms)';
COMMENT ON COLUMN excel_upload_histories.excluded_count IS 'ExcelUploadManagementService.uploadExcel 시 오류가 있으면 전체 반영 제외로 산출';
COMMENT ON COLUMN excel_upload_histories.saved_count IS 'ExcelUploadManagementService.uploadExcel 시 모든 행 정상일 때만 저장 처리된 행 수로 산출';
COMMENT ON COLUMN excel_upload_histories.upload_status IS 'SUCCESS:성공|FAILED:검증실패';
COMMENT ON COLUMN excel_upload_histories.uploaded_at IS 'ExcelUploadManagementService.uploadExcel 시 애플리케이션 요청 처리 시점에 DB now()로 기록';
COMMENT ON COLUMN excel_upload_errors.upload_id IS 'excel_upload_histories.upload_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN excel_upload_errors.input_value IS '검증 오류 발생 시 사용자 입력 원문값이며 개인정보 포함을 피하도록 업무 템플릿에서 제한';

CREATE INDEX IF NOT EXISTS ix_excel_upload_histories_template_id ON excel_upload_histories (template_id);
CREATE INDEX IF NOT EXISTS ix_excel_upload_histories_uploader_id ON excel_upload_histories (uploader_id);
CREATE INDEX IF NOT EXISTS ix_excel_upload_histories_upload_status ON excel_upload_histories (upload_status);
CREATE INDEX IF NOT EXISTS ix_excel_upload_histories_uploaded_at ON excel_upload_histories (uploaded_at DESC);
CREATE INDEX IF NOT EXISTS ix_excel_upload_errors_upload_id ON excel_upload_errors (upload_id);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_excel_upload_histories_counts_non_negative'
    ) THEN
        ALTER TABLE excel_upload_histories ADD CONSTRAINT ck_excel_upload_histories_counts_non_negative CHECK (
            total_count >= 0 AND success_count >= 0 AND error_count >= 0 AND excluded_count >= 0 AND saved_count >= 0 AND processing_time_ms >= 0
        );
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_excel_upload_histories_status'
    ) THEN
        ALTER TABLE excel_upload_histories ADD CONSTRAINT ck_excel_upload_histories_status CHECK (upload_status IN ('SUCCESS', 'FAILED'));
    END IF;
END $$;

INSERT INTO menus (menu_id, parent_menu_id, menu_name, screen_id, url, display_order)
VALUES ('M-EXCEL-UPLOAD', 'M-FILE', '엑셀 업로드', 'SCR-EXCEL-UPLOAD', '/admin/excel/uploads', 50)
ON CONFLICT (menu_id) DO UPDATE SET
    parent_menu_id = EXCLUDED.parent_menu_id,
    menu_name = EXCLUDED.menu_name,
    screen_id = EXCLUDED.screen_id,
    url = EXCLUDED.url,
    display_order = EXCLUDED.display_order;

INSERT INTO excel_upload_histories (
    template_id, uploader_id, file_name, total_count, success_count, error_count,
    excluded_count, saved_count, processing_time_ms, upload_status, uploaded_at
)
SELECT et.template_id, 'admin', '교수업적_업로드_정상_2026.xlsx', 3, 3, 0, 0, 3, 1250, 'SUCCESS', TIMESTAMP '2026-08-16 13:00:00'
FROM excel_templates et
WHERE et.business_area = 'ACHIEVEMENT'
  AND et.version = '2026.1'
  AND NOT EXISTS (SELECT 1 FROM excel_upload_histories WHERE file_name = '교수업적_업로드_정상_2026.xlsx');

INSERT INTO excel_upload_histories (
    template_id, uploader_id, file_name, total_count, success_count, error_count,
    excluded_count, saved_count, processing_time_ms, upload_status, uploaded_at
)
SELECT et.template_id, 'admin', '교수업적_업로드_오류_2026.xlsx', 2, 0, 1, 2, 0, 980, 'FAILED', TIMESTAMP '2026-08-16 13:10:00'
FROM excel_templates et
WHERE et.business_area = 'ACHIEVEMENT'
  AND et.version = '2026.1'
  AND NOT EXISTS (SELECT 1 FROM excel_upload_histories WHERE file_name = '교수업적_업로드_오류_2026.xlsx');

INSERT INTO excel_upload_errors (upload_id, row_number, column_name, input_value, error_code, error_reason)
SELECT euh.upload_id, 3, '점수', 'abc', 'TYPE', '점수 값은 숫자 형식이어야 합니다.'
FROM excel_upload_histories euh
WHERE euh.file_name = '교수업적_업로드_오류_2026.xlsx'
  AND NOT EXISTS (SELECT 1 FROM excel_upload_errors WHERE upload_id = euh.upload_id AND row_number = 3 AND column_name = '점수');
