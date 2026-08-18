-- Attachment management vertical slice: metadata integrity, logical delete boundaries, indexes, menu, and seed references.
ALTER TABLE attachment_files ADD COLUMN IF NOT EXISTS size_bytes bigint NOT NULL DEFAULT 0;
ALTER TABLE attachment_files ADD COLUMN IF NOT EXISTS uploaded_by varchar(50) NOT NULL DEFAULT 'admin';
ALTER TABLE attachment_files ADD COLUMN IF NOT EXISTS uploaded_at timestamp NOT NULL DEFAULT now();
ALTER TABLE attachment_files ADD COLUMN IF NOT EXISTS finalized_record boolean NOT NULL DEFAULT false;
ALTER TABLE attachment_files ADD COLUMN IF NOT EXISTS storage_present boolean NOT NULL DEFAULT true;
ALTER TABLE attachment_files ADD COLUMN IF NOT EXISTS integrity_status varchar(30) NOT NULL DEFAULT 'OK';
ALTER TABLE attachment_files ADD COLUMN IF NOT EXISTS delete_reason text;
ALTER TABLE attachment_files ADD COLUMN IF NOT EXISTS deleted_by varchar(50);
ALTER TABLE attachment_files ADD COLUMN IF NOT EXISTS deleted_at timestamp;

COMMENT ON COLUMN attachment_files.attachment_id IS 'attachment_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN attachment_files.business_key IS '업무자료 연결 키이며 AttachmentManagementService.saveAttachment에서 변경하지 않는 생명주기 식별자';
COMMENT ON COLUMN attachment_files.original_name IS '업로드 시 애플리케이션에서 기록한 원본 파일명';
COMMENT ON COLUMN attachment_files.stored_name IS 'FileStoragePort 저장 시 애플리케이션에서 기록한 Docker named volume 내부 저장명';
COMMENT ON COLUMN attachment_files.extension IS '업로드 시 애플리케이션에서 추출한 확장자';
COMMENT ON COLUMN attachment_files.size_bytes IS 'FileStoragePort 저장 시 애플리케이션에서 기록한 파일 크기 바이트';
COMMENT ON COLUMN attachment_files.uploaded_by IS 'user_accounts.user_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN attachment_files.uploaded_at IS 'FileStoragePort 저장 시 애플리케이션에서 기록한 업로드 일시';
COMMENT ON COLUMN attachment_files.malware_scan_result IS 'PENDING:대기|CLEAN:정상|INFECTED:감염|FAILED:실패';
COMMENT ON COLUMN attachment_files.deleted IS 'AttachmentManagementService.saveAttachment 시 애플리케이션에서 갱신되는 논리삭제 여부';
COMMENT ON COLUMN attachment_files.finalized_record IS '평가확정 자료 여부이며 true이면 AttachmentManagementService.saveAttachment에서 논리삭제를 차단';
COMMENT ON COLUMN attachment_files.storage_present IS 'FileStoragePort 정합성 점검 시 애플리케이션에서 갱신되는 실제 파일 존재 여부';
COMMENT ON COLUMN attachment_files.integrity_status IS 'OK:정상|MISSING_BUSINESS:연결자료없음|MISSING_FILE:실제파일없음|DUPLICATE:중복파일';
COMMENT ON COLUMN attachment_files.delete_reason IS 'AttachmentManagementService.saveAttachment 논리삭제 시 입력받아 애플리케이션에서 갱신';
COMMENT ON COLUMN attachment_files.deleted_by IS 'user_accounts.user_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN attachment_files.deleted_at IS 'AttachmentManagementService.saveAttachment 논리삭제 시 애플리케이션에서 갱신';

CREATE INDEX IF NOT EXISTS ix_attachment_files_business_key ON attachment_files (business_key);
CREATE INDEX IF NOT EXISTS ix_attachment_files_scan_deleted ON attachment_files (malware_scan_result, deleted);
CREATE INDEX IF NOT EXISTS ix_attachment_files_integrity_status ON attachment_files (integrity_status);
CREATE INDEX IF NOT EXISTS ix_attachment_files_uploaded_at ON attachment_files (uploaded_at DESC);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_attachment_files_size_non_negative'
    ) THEN
        ALTER TABLE attachment_files ADD CONSTRAINT ck_attachment_files_size_non_negative CHECK (size_bytes >= 0);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_attachment_files_delete_reason_required'
    ) THEN
        ALTER TABLE attachment_files ADD CONSTRAINT ck_attachment_files_delete_reason_required CHECK (deleted = false OR delete_reason IS NOT NULL);
    END IF;
END $$;

INSERT INTO menus (menu_id, parent_menu_id, menu_name, screen_id, url, display_order)
VALUES ('M-ATTACHMENT', 'M-FILE', '첨부파일 관리', 'SCR-ATTACHMENT', '/admin/files/attachments', 30)
ON CONFLICT (menu_id) DO UPDATE SET
    parent_menu_id = EXCLUDED.parent_menu_id,
    menu_name = EXCLUDED.menu_name,
    screen_id = EXCLUDED.screen_id,
    url = EXCLUDED.url,
    display_order = EXCLUDED.display_order;

INSERT INTO attachment_files (business_key, original_name, stored_name, extension, size_bytes, uploaded_by, uploaded_at, malware_scan_result, deleted, finalized_record, storage_present, integrity_status)
SELECT 'NOTICE:1', '평가일정 안내.pdf', '2026/08/notice-1.pdf', 'pdf', 204800, 'admin', TIMESTAMP '2026-08-16 09:30:00', 'CLEAN', false, false, true, 'OK'
WHERE NOT EXISTS (SELECT 1 FROM attachment_files WHERE business_key = 'NOTICE:1' AND original_name = '평가일정 안내.pdf');

INSERT INTO attachment_files (business_key, original_name, stored_name, extension, size_bytes, uploaded_by, uploaded_at, malware_scan_result, deleted, finalized_record, storage_present, integrity_status)
SELECT 'NOTICE:2', '중복 제출 파일.xlsx', '2026/08/notice-duplicate.xlsx', 'xlsx', 102400, 'admin', TIMESTAMP '2026-08-16 10:00:00', 'PENDING', false, false, true, 'DUPLICATE'
WHERE NOT EXISTS (SELECT 1 FROM attachment_files WHERE business_key = 'NOTICE:2' AND original_name = '중복 제출 파일.xlsx');

INSERT INTO attachment_files (business_key, original_name, stored_name, extension, size_bytes, uploaded_by, uploaded_at, malware_scan_result, deleted, finalized_record, storage_present, integrity_status)
SELECT 'FINAL:2026', '확정 평가자료.pdf', '2026/08/final-evaluation.pdf', 'pdf', 409600, 'admin', TIMESTAMP '2026-08-16 11:00:00', 'CLEAN', false, true, true, 'OK'
WHERE NOT EXISTS (SELECT 1 FROM attachment_files WHERE business_key = 'FINAL:2026' AND original_name = '확정 평가자료.pdf');
