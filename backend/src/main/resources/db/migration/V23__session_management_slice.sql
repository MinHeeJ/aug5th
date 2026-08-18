-- Session management vertical slice: active session listing, forced termination support, menu, indexes, and seed references.
COMMENT ON COLUMN user_sessions.session_id IS 'user_sessions.session_id 생명주기 식별자';
COMMENT ON COLUMN user_sessions.user_id IS 'user_accounts.user_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN user_sessions.login_at IS 'AuthService.login 시 생성된 최초 로그인 시각';
COMMENT ON COLUMN user_sessions.last_activity_at IS 'AuthService.login/logout 또는 SessionManagementService.terminate 시 애플리케이션에서 갱신';
COMMENT ON COLUMN user_sessions.ip_address IS 'AuthService.login 시 기록된 접속 IP 주소';
COMMENT ON COLUMN user_sessions.session_status IS 'ACTIVE:활성|LOGOUT:로그아웃|IDLE_EXPIRED:유휴만료|ABSOLUTE_EXPIRED:절대만료|TERMINATED:관리자강제종료';
COMMENT ON COLUMN session_termination_histories.termination_id IS 'session_termination_histories.termination_id 생명주기 식별자';
COMMENT ON COLUMN session_termination_histories.session_id IS 'user_sessions.session_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN session_termination_histories.termination_type IS 'LOGOUT:로그아웃|IDLE_EXPIRED:유휴만료|ABSOLUTE_EXPIRED:절대만료|FORCED:관리자강제종료';
COMMENT ON COLUMN session_termination_histories.reason IS 'AuthService.logout 또는 SessionManagementService.terminate 시 종료 사유로 기록되며 수정·삭제하지 않음';
COMMENT ON COLUMN session_termination_histories.terminated_by IS 'user_accounts.user_id 참조 의도 (FK 미선언)';

CREATE INDEX IF NOT EXISTS ix_user_sessions_last_activity_at ON user_sessions (last_activity_at DESC);
CREATE INDEX IF NOT EXISTS ix_user_sessions_ip_address ON user_sessions (ip_address);
CREATE INDEX IF NOT EXISTS ix_session_termination_histories_session_id ON session_termination_histories (session_id);
CREATE INDEX IF NOT EXISTS ix_session_termination_histories_terminated_at ON session_termination_histories (terminated_at DESC);

INSERT INTO menus (menu_id, parent_menu_id, menu_name, screen_id, url, display_order)
VALUES ('M-SESSION', 'M-SECURITY', '접속현황 관리', 'SCR-SESSION', '/admin/security/sessions', 80)
ON CONFLICT (menu_id) DO UPDATE SET
    parent_menu_id = EXCLUDED.parent_menu_id,
    menu_name = EXCLUDED.menu_name,
    screen_id = EXCLUDED.screen_id,
    url = EXCLUDED.url,
    display_order = EXCLUDED.display_order;

INSERT INTO menu_permissions (target_type, target_id, menu_id, allowed)
SELECT 'ROLE', role_code, 'M-SESSION', role_code = 'R09'
FROM roles
WHERE NOT EXISTS (
    SELECT 1 FROM menu_permissions mp
    WHERE mp.target_type = 'ROLE'
      AND mp.target_id = roles.role_code
      AND mp.menu_id = 'M-SESSION'
);

INSERT INTO user_sessions (session_id, user_id, login_at, last_activity_at, ip_address, session_status)
VALUES
    ('SEED-ACTIVE-ADMIN', 'admin', timestamp '2026-08-16 09:00:00', timestamp '2026-08-16 09:30:00', '127.0.0.1', 'ACTIVE'),
    ('SEED-LOGOUT-ADMIN', 'admin', timestamp '2026-08-15 09:00:00', timestamp '2026-08-15 10:00:00', '127.0.0.1', 'LOGOUT')
ON CONFLICT (session_id) DO UPDATE SET
    user_id = EXCLUDED.user_id,
    login_at = EXCLUDED.login_at,
    last_activity_at = EXCLUDED.last_activity_at,
    ip_address = EXCLUDED.ip_address,
    session_status = EXCLUDED.session_status;

INSERT INTO session_termination_histories (session_id, termination_type, reason, terminated_by, terminated_at)
SELECT 'SEED-LOGOUT-ADMIN', 'LOGOUT', '시드 로그아웃 종료이력', 'admin', timestamp '2026-08-15 10:00:00'
WHERE NOT EXISTS (
    SELECT 1 FROM session_termination_histories
    WHERE session_id = 'SEED-LOGOUT-ADMIN'
      AND termination_type = 'LOGOUT'
      AND terminated_at = timestamp '2026-08-15 10:00:00'
);
