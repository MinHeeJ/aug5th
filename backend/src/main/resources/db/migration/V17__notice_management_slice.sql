-- Notice management vertical slice: publication target, period validation, indexes, menu, and seed references.
ALTER TABLE notices ADD COLUMN IF NOT EXISTS content text NOT NULL DEFAULT '';
ALTER TABLE notices ADD COLUMN IF NOT EXISTS target_organizations varchar(200);
ALTER TABLE notices ADD COLUMN IF NOT EXISTS attachment_count integer NOT NULL DEFAULT 0;
ALTER TABLE notices ADD COLUMN IF NOT EXISTS enabled boolean NOT NULL DEFAULT true;
ALTER TABLE notices ADD COLUMN IF NOT EXISTS created_at timestamp NOT NULL DEFAULT now();
ALTER TABLE notices ADD COLUMN IF NOT EXISTS updated_at timestamp;

COMMENT ON COLUMN notices.notice_id IS 'notice_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN notices.title IS 'NoticeManagementService.saveNotice 시 애플리케이션에서 갱신되는 공지 제목';
COMMENT ON COLUMN notices.content IS 'NoticeManagementService.saveNotice 시 애플리케이션에서 갱신되는 공지 본문 요약 원천';
COMMENT ON COLUMN notices.post_from IS 'NoticeManagementService.saveNotice 시 애플리케이션에서 갱신되는 게시 시작일';
COMMENT ON COLUMN notices.post_to IS 'NoticeManagementService.saveNotice 시 애플리케이션에서 갱신되는 게시 종료일';
COMMENT ON COLUMN notices.target_roles IS '지정 대상 역할 목록, 쉼표 구분 R01~R09';
COMMENT ON COLUMN notices.target_organizations IS '지정 대상 조직코드 목록, 쉼표 구분 organizations.organization_code 참조 의도 (FK 미선언)';
COMMENT ON COLUMN notices.important IS 'NoticeManagementService.saveNotice 시 애플리케이션에서 갱신되는 중요 공지 여부';
COMMENT ON COLUMN notices.attachment_count IS 'AttachmentService 연동 후 첨부파일 메타데이터 기준으로 갱신 예정인 첨부파일 수';
COMMENT ON COLUMN notices.enabled IS 'NoticeManagementService.saveNotice 시 애플리케이션에서 갱신되는 공지 사용 여부';
COMMENT ON COLUMN notices.created_at IS 'NoticeManagementService.create 시 애플리케이션에서 갱신되는 생성일시';
COMMENT ON COLUMN notices.updated_at IS 'NoticeManagementService.saveNotice 시 애플리케이션에서 갱신되는 수정일시';

CREATE INDEX IF NOT EXISTS ix_notices_post_period ON notices (post_from, post_to);
CREATE INDEX IF NOT EXISTS ix_notices_target_roles ON notices (target_roles);
CREATE INDEX IF NOT EXISTS ix_notices_enabled_important ON notices (enabled, important);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_notices_post_period'
    ) THEN
        ALTER TABLE notices ADD CONSTRAINT ck_notices_post_period CHECK (post_from <= post_to);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_notices_attachment_count_non_negative'
    ) THEN
        ALTER TABLE notices ADD CONSTRAINT ck_notices_attachment_count_non_negative CHECK (attachment_count >= 0);
    END IF;
END $$;

INSERT INTO menus (menu_id, parent_menu_id, menu_name, screen_id, url, display_order)
VALUES ('M-NOTICE', 'M-SYSTEM', '공지사항 관리', 'SCR-NOTICE', '/admin/notices', 25)
ON CONFLICT (menu_id) DO UPDATE SET
    parent_menu_id = EXCLUDED.parent_menu_id,
    menu_name = EXCLUDED.menu_name,
    screen_id = EXCLUDED.screen_id,
    url = EXCLUDED.url,
    display_order = EXCLUDED.display_order;

INSERT INTO notices (title, content, post_from, post_to, target_roles, target_organizations, important, attachment_count, enabled)
SELECT '2026학년도 교수업적평가 공통 일정 안내', '평가일정과 시스템 점검 기간을 확인하세요.', DATE '2026-01-01', DATE '2026-12-31', 'R01,R09', 'KNUE-EDU', true, 0, true
WHERE NOT EXISTS (SELECT 1 FROM notices WHERE title = '2026학년도 교수업적평가 공통 일정 안내');

INSERT INTO notices (title, content, post_from, post_to, target_roles, target_organizations, important, attachment_count, enabled)
SELECT '공통기능 파일정책 적용 안내', '공지 열람은 업무 승인이나 확인처리로 간주하지 않습니다.', DATE '2026-03-01', DATE '2026-08-31', 'R09', 'KNUE', false, 0, true
WHERE NOT EXISTS (SELECT 1 FROM notices WHERE title = '공통기능 파일정책 적용 안내');
