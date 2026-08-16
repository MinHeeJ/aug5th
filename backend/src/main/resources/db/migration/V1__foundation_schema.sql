-- Foundation schema generated from durable data-model contract. Do not reference runner input paths at runtime.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS user_accounts (
    user_id varchar(50) NOT NULL,
    enabled boolean NOT NULL,
    role_summary varchar(200),
    created_at timestamp NOT NULL,
    updated_at timestamp,
    status varchar(20) NOT NULL,
    password_hash varchar(128),
    CONSTRAINT pk_user_accounts PRIMARY KEY (user_id)
);
COMMENT ON TABLE user_accounts IS '내부 사용자 계정과 시스템 사용 상태를 관리한다. KORUS 원천 인사정보와 분리하여 로컬 권한 부여의 기준이 된다.';
COMMENT ON COLUMN user_accounts.user_id IS 'user_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN user_accounts.role_summary IS '애플리케이션 서비스 처리 시 갱신되는 파생 또는 조건 데이터';
COMMENT ON COLUMN user_accounts.status IS 'ACTIVE:사용|INACTIVE:미사용|LOCKED:잠김';
COMMENT ON COLUMN user_accounts.password_hash IS 'AuthenticationService.login 시 검증하는 로컬 개발용 비밀번호 해시';

CREATE TABLE IF NOT EXISTS korus_personnel_snapshots (
    person_id varchar(50) NOT NULL,
    employee_no varchar(50) NOT NULL,
    name_encrypted text,
    department_code varchar(50),
    rank_name varchar(100),
    employment_status varchar(30) NOT NULL,
    CONSTRAINT pk_korus_personnel_snapshots PRIMARY KEY (person_id)
);
COMMENT ON TABLE korus_personnel_snapshots IS 'KORUS 교직원 원천정보의 로컬 Mock snapshot이다. 직접 수정하지 않고 조회와 동기화 확인에 사용한다.';
COMMENT ON COLUMN korus_personnel_snapshots.person_id IS 'person_id 참조 의도 (FK 미선언)';

CREATE TABLE IF NOT EXISTS organizations (
    organization_code varchar(50) NOT NULL,
    organization_name varchar(200) NOT NULL,
    parent_organization_code varchar(50),
    valid_from date NOT NULL,
    valid_to date,
    enabled boolean NOT NULL,
    CONSTRAINT pk_organizations PRIMARY KEY (organization_code)
);
COMMENT ON TABLE organizations IS 'KORUS 조직 구조의 로컬 Mock snapshot과 관리용 조직 상태를 보관한다. 조직 계층과 유효기간 조회의 기준이다.';

CREATE TABLE IF NOT EXISTS position_assignments (
    position_id bigserial NOT NULL,
    position_code varchar(50) NOT NULL,
    user_id varchar(50) NOT NULL,
    organization_code varchar(50) NOT NULL,
    valid_from date NOT NULL,
    valid_to date,
    CONSTRAINT pk_position_assignments PRIMARY KEY (position_id)
);
COMMENT ON TABLE position_assignments IS '사용자 보직과 조직 사용자 매핑을 보관한다. 기간 기반 보직 조회와 권한 범위 판단에 사용한다.';
COMMENT ON COLUMN position_assignments.position_id IS 'position_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN position_assignments.user_id IS 'user_id 참조 의도 (FK 미선언)';

CREATE TABLE IF NOT EXISTS roles (
    role_code varchar(10) NOT NULL,
    role_name varchar(100) NOT NULL,
    purpose text,
    grant_criteria text,
    default_data_scope varchar(30) NOT NULL,
    enabled boolean NOT NULL,
    CONSTRAINT pk_roles PRIMARY KEY (role_code)
);
COMMENT ON TABLE roles IS 'R01부터 R09까지의 업무 역할과 기본 데이터 범위를 정의한다. 메뉴·기능·데이터 권한 부여의 기준이다.';

CREATE TABLE IF NOT EXISTS user_roles (
    user_role_id bigserial NOT NULL,
    user_id varchar(50) NOT NULL,
    role_code varchar(10) NOT NULL,
    valid_from date NOT NULL,
    valid_to date,
    approver_id varchar(50),
    CONSTRAINT pk_user_roles PRIMARY KEY (user_role_id)
);
COMMENT ON TABLE user_roles IS '사용자에게 부여된 역할과 유효기간을 보관한다. 인증 후 권한 판정과 사용자 역할 관리에 사용한다.';
COMMENT ON COLUMN user_roles.user_role_id IS 'user_role_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN user_roles.user_id IS 'user_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN user_roles.approver_id IS 'approver_id 참조 의도 (FK 미선언)';

CREATE TABLE IF NOT EXISTS menus (
    menu_id varchar(50) NOT NULL,
    parent_menu_id varchar(50),
    menu_name varchar(200) NOT NULL,
    screen_id varchar(80) NOT NULL,
    url varchar(300) NOT NULL,
    display_order integer NOT NULL,
    CONSTRAINT pk_menus PRIMARY KEY (menu_id)
);
COMMENT ON TABLE menus IS '공통기능 25개 화면의 메뉴 트리와 라우트를 정의한다. 프론트엔드 메뉴 표시와 서버 메뉴 권한 판정의 기준이다.';
COMMENT ON COLUMN menus.menu_id IS 'menu_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN menus.parent_menu_id IS 'parent_menu_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN menus.screen_id IS 'screen_id 참조 의도 (FK 미선언)';

CREATE TABLE IF NOT EXISTS menu_permissions (
    menu_permission_id bigserial NOT NULL,
    target_type varchar(20) NOT NULL,
    target_id varchar(80) NOT NULL,
    menu_id varchar(50) NOT NULL,
    allowed boolean NOT NULL,
    CONSTRAINT pk_menu_permissions PRIMARY KEY (menu_permission_id)
);
COMMENT ON TABLE menu_permissions IS '역할 또는 사용자 대상 메뉴 접근 허용 여부를 보관한다. 권한 없는 메뉴 숨김과 서버 차단에 사용한다.';
COMMENT ON COLUMN menu_permissions.menu_permission_id IS 'menu_permission_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN menu_permissions.target_id IS 'target_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN menu_permissions.menu_id IS 'menu_id 참조 의도 (FK 미선언)';

CREATE TABLE IF NOT EXISTS function_permissions (
    function_permission_id bigserial NOT NULL,
    role_code varchar(10) NOT NULL,
    screen_id varchar(80) NOT NULL,
    action_code varchar(30) NOT NULL,
    allowed boolean NOT NULL,
    CONSTRAINT pk_function_permissions PRIMARY KEY (function_permission_id)
);
COMMENT ON TABLE function_permissions IS '화면별 조회·등록·수정·삭제 등 기능 액션 권한을 역할 단위로 보관한다. 서버의 기능 실행 차단에 사용한다.';
COMMENT ON COLUMN function_permissions.function_permission_id IS 'function_permission_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN function_permissions.screen_id IS 'screen_id 참조 의도 (FK 미선언)';

CREATE TABLE IF NOT EXISTS data_scope_permissions (
    data_scope_id bigserial NOT NULL,
    role_code varchar(10) NOT NULL,
    scope_type varchar(30) NOT NULL,
    organization_code varchar(50),
    business_area varchar(80),
    CONSTRAINT pk_data_scope_permissions PRIMARY KEY (data_scope_id)
);
COMMENT ON TABLE data_scope_permissions IS '역할별 데이터 범위 권한을 보관한다. 목록 조회 시 본인·부서·전체 등 서버 조회조건 강제에 사용한다.';
COMMENT ON COLUMN data_scope_permissions.data_scope_id IS 'data_scope_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN data_scope_permissions.scope_type IS 'SELF:본인|DEPARTMENT:소속학과|COLLEGE:단과대학|BUSINESS:담당업무|ALL:전체';

CREATE TABLE IF NOT EXISTS code_groups (
    group_id varchar(50) NOT NULL,
    group_name varchar(200) NOT NULL,
    description text,
    managing_department varchar(100),
    enabled boolean NOT NULL,
    CONSTRAINT pk_code_groups PRIMARY KEY (group_id)
);
COMMENT ON TABLE code_groups IS '공통코드 그룹을 관리한다. 상세코드 묶음과 관리부서 기준 조회에 사용한다.';
COMMENT ON COLUMN code_groups.group_id IS 'group_id 참조 의도 (FK 미선언)';

CREATE TABLE IF NOT EXISTS code_details (
    code_detail_id bigserial NOT NULL,
    group_id varchar(50) NOT NULL,
    code_value varchar(80) NOT NULL,
    code_name varchar(200) NOT NULL,
    parent_code_value varchar(80),
    display_order integer NOT NULL,
    CONSTRAINT pk_code_details PRIMARY KEY (code_detail_id)
);
COMMENT ON TABLE code_details IS '공통코드 상세값과 정렬·계층 정보를 관리한다. 화면 선택항목과 서버 검증 기준으로 사용한다.';
COMMENT ON COLUMN code_details.code_detail_id IS 'code_detail_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN code_details.group_id IS 'group_id 참조 의도 (FK 미선언)';

CREATE TABLE IF NOT EXISTS system_configurations (
    config_key varchar(80) NOT NULL,
    config_value varchar(300) NOT NULL,
    unit varchar(30) NOT NULL,
    value_range varchar(100),
    enabled boolean NOT NULL,
    CONSTRAINT pk_system_configurations PRIMARY KEY (config_key)
);
COMMENT ON TABLE system_configurations IS '공통 환경설정 키와 값을 관리한다. 런타임 정책값 조회와 설정 변경 이력의 기준이다.';

CREATE TABLE IF NOT EXISTS base_years (
    base_year char(4) NOT NULL,
    default_query_year char(4) NOT NULL,
    copy_baseline_enabled boolean NOT NULL,
    reset_enabled boolean NOT NULL,
    enabled boolean NOT NULL,
    CONSTRAINT pk_base_years PRIMARY KEY (base_year)
);
COMMENT ON TABLE base_years IS '기준연도와 기본 조회연도 정책을 관리한다. 연도별 공통기능 초기값 판단에 사용한다.';

CREATE TABLE IF NOT EXISTS file_policies (
    file_policy_id bigserial NOT NULL,
    business_area varchar(80) NOT NULL,
    allowed_extensions varchar(200) NOT NULL,
    max_file_size_mb integer NOT NULL,
    max_file_count integer NOT NULL,
    malware_scan_enabled boolean NOT NULL,
    CONSTRAINT pk_file_policies PRIMARY KEY (file_policy_id)
);
COMMENT ON TABLE file_policies IS '업무영역별 파일 확장자·크기·개수·악성코드 검사 정책을 관리한다. 첨부파일 검증의 기준이다.';
COMMENT ON COLUMN file_policies.file_policy_id IS 'file_policy_id 참조 의도 (FK 미선언)';

CREATE TABLE IF NOT EXISTS notices (
    notice_id bigserial NOT NULL,
    title varchar(300) NOT NULL,
    post_from date NOT NULL,
    post_to date NOT NULL,
    target_roles varchar(200),
    important boolean NOT NULL,
    CONSTRAINT pk_notices PRIMARY KEY (notice_id)
);
COMMENT ON TABLE notices IS '공지사항 게시기간과 대상 역할을 관리한다. 관리자 공지 조회와 표시 정책에 사용한다.';
COMMENT ON COLUMN notices.notice_id IS 'notice_id 참조 의도 (FK 미선언)';

CREATE TABLE IF NOT EXISTS attachment_files (
    attachment_id bigserial NOT NULL,
    business_key varchar(100) NOT NULL,
    original_name varchar(300) NOT NULL,
    stored_name varchar(300) NOT NULL,
    extension varchar(20) NOT NULL,
    malware_scan_result varchar(30) NOT NULL,
    deleted boolean NOT NULL,
    CONSTRAINT pk_attachment_files PRIMARY KEY (attachment_id)
);
COMMENT ON TABLE attachment_files IS '첨부파일 저장명과 악성코드 검사·논리삭제 메타데이터를 보관한다. 실제 파일은 Docker named volume에 저장된다.';
COMMENT ON COLUMN attachment_files.attachment_id IS 'attachment_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN attachment_files.malware_scan_result IS 'PENDING:대기|CLEAN:정상|INFECTED:감염|FAILED:실패';

CREATE TABLE IF NOT EXISTS excel_templates (
    template_id bigserial NOT NULL,
    business_area varchar(80) NOT NULL,
    version varchar(30) NOT NULL,
    required_columns jsonb NOT NULL,
    effective_date date NOT NULL,
    download_file_id bigint,
    CONSTRAINT pk_excel_templates PRIMARY KEY (template_id)
);
COMMENT ON TABLE excel_templates IS '업무별 엑셀 업로드 템플릿 버전과 필수 컬럼 정의를 보관한다. 업로드 검증과 양식 다운로드에 사용한다.';
COMMENT ON COLUMN excel_templates.template_id IS 'template_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN excel_templates.required_columns IS '애플리케이션 서비스 처리 시 갱신되는 파생 또는 조건 데이터';
COMMENT ON COLUMN excel_templates.download_file_id IS 'download_file_id 참조 의도 (FK 미선언)';

CREATE TABLE IF NOT EXISTS excel_upload_histories (
    upload_id bigserial NOT NULL,
    template_id bigint NOT NULL,
    uploader_id varchar(50) NOT NULL,
    file_name varchar(300) NOT NULL,
    total_count integer NOT NULL,
    success_count integer NOT NULL,
    error_count integer NOT NULL,
    processing_time_ms integer NOT NULL,
    CONSTRAINT pk_excel_upload_histories PRIMARY KEY (upload_id)
);
COMMENT ON TABLE excel_upload_histories IS '엑셀 업로드 처리 건수와 처리시간 이력을 보관한다. 업로드 결과 조회와 오류 파일 생성 기준이다.';
COMMENT ON COLUMN excel_upload_histories.upload_id IS 'upload_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN excel_upload_histories.template_id IS 'template_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN excel_upload_histories.uploader_id IS 'uploader_id 참조 의도 (FK 미선언)';

CREATE TABLE IF NOT EXISTS excel_upload_errors (
    upload_error_id bigserial NOT NULL,
    upload_id bigint NOT NULL,
    row_number integer NOT NULL,
    column_name varchar(100) NOT NULL,
    input_value text,
    error_code varchar(50) NOT NULL,
    error_reason text NOT NULL,
    CONSTRAINT pk_excel_upload_errors PRIMARY KEY (upload_error_id)
);
COMMENT ON TABLE excel_upload_errors IS '엑셀 업로드 행·컬럼별 검증 오류를 보관한다. 사용자 오류파일과 재검증 안내에 사용한다.';
COMMENT ON COLUMN excel_upload_errors.upload_error_id IS 'upload_error_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN excel_upload_errors.upload_id IS 'upload_id 참조 의도 (FK 미선언)';

CREATE TABLE IF NOT EXISTS excel_download_requests (
    download_id bigserial NOT NULL,
    requester_id varchar(50) NOT NULL,
    query_condition jsonb NOT NULL,
    data_scope_applied jsonb NOT NULL,
    file_id bigint,
    created_at timestamp NOT NULL,
    CONSTRAINT pk_excel_download_requests PRIMARY KEY (download_id)
);
COMMENT ON TABLE excel_download_requests IS '엑셀 다운로드 요청 조건과 적용된 데이터 범위를 보관한다. 개인정보·권한 감사와 파일 생성 추적에 사용한다.';
COMMENT ON COLUMN excel_download_requests.download_id IS 'download_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN excel_download_requests.requester_id IS 'requester_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN excel_download_requests.query_condition IS '애플리케이션 서비스 처리 시 갱신되는 파생 또는 조건 데이터';
COMMENT ON COLUMN excel_download_requests.data_scope_applied IS '애플리케이션 서비스 처리 시 갱신되는 파생 또는 조건 데이터';
COMMENT ON COLUMN excel_download_requests.file_id IS 'file_id 참조 의도 (FK 미선언)';

CREATE TABLE IF NOT EXISTS privacy_field_policies (
    field_policy_id bigserial NOT NULL,
    field_name varchar(100) NOT NULL,
    privacy_grade varchar(20) NOT NULL,
    encryption_enabled boolean NOT NULL,
    masking_rule varchar(100) NOT NULL,
    log_excluded boolean NOT NULL,
    CONSTRAINT pk_privacy_field_policies PRIMARY KEY (field_policy_id)
);
COMMENT ON TABLE privacy_field_policies IS '개인정보 필드별 암호화·마스킹·로그 제외 정책을 보관한다. 개인정보 처리 화면과 API 검증의 기준이다.';
COMMENT ON COLUMN privacy_field_policies.field_policy_id IS 'field_policy_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN privacy_field_policies.privacy_grade IS 'PUBLIC:일반|PERSONAL:개인정보|SENSITIVE:민감정보';

CREATE TABLE IF NOT EXISTS privacy_access_permissions (
    privacy_permission_id bigserial NOT NULL,
    role_code varchar(10) NOT NULL,
    access_type varchar(30) NOT NULL,
    allowed boolean NOT NULL,
    CONSTRAINT pk_privacy_access_permissions PRIMARY KEY (privacy_permission_id)
);
COMMENT ON TABLE privacy_access_permissions IS '역할별 개인정보 조회·복호화·내보내기 허용 여부를 보관한다. 민감정보 접근 차단에 사용한다.';
COMMENT ON COLUMN privacy_access_permissions.privacy_permission_id IS 'privacy_permission_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN privacy_access_permissions.access_type IS 'MASKED:마스킹조회|DECRYPT:복호화조회|EXPORT:내보내기';

CREATE TABLE IF NOT EXISTS privacy_access_histories (
    privacy_history_id bigserial NOT NULL,
    actor_id varchar(50) NOT NULL,
    subject_id varchar(50) NOT NULL,
    purpose text NOT NULL,
    processed_at timestamp NOT NULL,
    ip_address varchar(45) NOT NULL,
    result varchar(30) NOT NULL,
    CONSTRAINT pk_privacy_access_histories PRIMARY KEY (privacy_history_id)
);
COMMENT ON TABLE privacy_access_histories IS '개인정보 조회·복호화 처리 이력을 보관한다. 목적·결과·접속 IP 기반 감사에 사용한다.';
COMMENT ON COLUMN privacy_access_histories.privacy_history_id IS 'privacy_history_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN privacy_access_histories.actor_id IS 'actor_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN privacy_access_histories.subject_id IS 'subject_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN privacy_access_histories.result IS 'SUCCESS:성공|DENIED:거부|FAILED:실패';

CREATE TABLE IF NOT EXISTS user_sessions (
    session_id varchar(100) NOT NULL,
    user_id varchar(50) NOT NULL,
    login_at timestamp NOT NULL,
    last_activity_at timestamp NOT NULL,
    ip_address varchar(45) NOT NULL,
    session_status varchar(30) NOT NULL,
    CONSTRAINT pk_user_sessions PRIMARY KEY (session_id)
);
COMMENT ON TABLE user_sessions IS '로그인 세션과 마지막 활동시각을 보관한다. 세션 쿠키 인증과 접속현황 조회의 기준이다.';
COMMENT ON COLUMN user_sessions.session_id IS 'session_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN user_sessions.user_id IS 'user_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN user_sessions.session_status IS 'ACTIVE:활성|LOGOUT:로그아웃|EXPIRED:만료|TERMINATED:강제종료';

CREATE TABLE IF NOT EXISTS session_termination_histories (
    termination_id bigserial NOT NULL,
    session_id varchar(100) NOT NULL,
    termination_type varchar(30) NOT NULL,
    reason text,
    terminated_by varchar(50),
    terminated_at timestamp NOT NULL,
    CONSTRAINT pk_session_termination_histories PRIMARY KEY (termination_id)
);
COMMENT ON TABLE session_termination_histories IS '로그아웃·만료·강제종료 이력을 보관한다. 세션 종료 원인 감사와 운영 추적에 사용한다.';
COMMENT ON COLUMN session_termination_histories.termination_id IS 'termination_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN session_termination_histories.session_id IS 'session_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN session_termination_histories.termination_type IS 'LOGOUT:로그아웃|EXPIRED:만료|FORCED:강제종료';
COMMENT ON COLUMN session_termination_histories.terminated_by IS 'terminated_by 참조 의도 (FK 미선언)';

CREATE TABLE IF NOT EXISTS audit_logs (
    audit_log_id bigserial NOT NULL,
    log_type varchar(30) NOT NULL,
    target_key varchar(120) NOT NULL,
    actor_id varchar(50) NOT NULL,
    before_value jsonb,
    after_value jsonb,
    result varchar(30) NOT NULL,
    CONSTRAINT pk_audit_logs PRIMARY KEY (audit_log_id)
);
COMMENT ON TABLE audit_logs IS '로그인·권한·변경 작업의 불변 감사로그를 보관한다. 변경 전후값과 결과 추적의 기준이다.';
COMMENT ON COLUMN audit_logs.audit_log_id IS 'audit_log_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN audit_logs.log_type IS 'LOGIN:로그인|LOGOUT:로그아웃|CREATE:등록|UPDATE:수정|DELETE:삭제|READ:조회|AUTHORIZATION:권한';
COMMENT ON COLUMN audit_logs.actor_id IS 'actor_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN audit_logs.before_value IS '애플리케이션 서비스 처리 시 갱신되는 파생 또는 조건 데이터';
COMMENT ON COLUMN audit_logs.after_value IS '애플리케이션 서비스 처리 시 갱신되는 파생 또는 조건 데이터';
COMMENT ON COLUMN audit_logs.result IS 'SUCCESS:성공|DENIED:거부|FAILED:실패';

CREATE TABLE IF NOT EXISTS batch_definitions (
    batch_id varchar(80) NOT NULL,
    schedule varchar(100) NOT NULL,
    predecessor_batch_id varchar(80),
    parameters jsonb,
    max_runtime_seconds integer NOT NULL,
    owner_id varchar(50) NOT NULL,
    CONSTRAINT pk_batch_definitions PRIMARY KEY (batch_id)
);
COMMENT ON TABLE batch_definitions IS '배치 정의와 스케줄·소유자를 관리한다. 운영 배치 실행 요청의 기준 정보이다.';
COMMENT ON COLUMN batch_definitions.batch_id IS 'batch_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN batch_definitions.predecessor_batch_id IS 'predecessor_batch_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN batch_definitions.parameters IS '애플리케이션 서비스 처리 시 갱신되는 파생 또는 조건 데이터';
COMMENT ON COLUMN batch_definitions.owner_id IS 'owner_id 참조 의도 (FK 미선언)';

CREATE TABLE IF NOT EXISTS batch_executions (
    batch_execution_id bigserial NOT NULL,
    batch_id varchar(80) NOT NULL,
    parameters jsonb,
    reason text NOT NULL,
    execution_status varchar(30) NOT NULL,
    requested_by varchar(50) NOT NULL,
    CONSTRAINT pk_batch_executions PRIMARY KEY (batch_execution_id)
);
COMMENT ON TABLE batch_executions IS '배치 실행 요청과 상태를 보관한다. 실행 이력과 재처리 판단에 사용한다.';
COMMENT ON COLUMN batch_executions.batch_execution_id IS 'batch_execution_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN batch_executions.batch_id IS 'batch_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN batch_executions.parameters IS '애플리케이션 서비스 처리 시 갱신되는 파생 또는 조건 데이터';
COMMENT ON COLUMN batch_executions.execution_status IS 'REQUESTED:요청|RUNNING:실행중|SUCCESS:성공|FAILED:실패|CANCELLED:취소';
COMMENT ON COLUMN batch_executions.requested_by IS 'requested_by 참조 의도 (FK 미선언)';

CREATE TABLE IF NOT EXISTS batch_results (
    batch_result_id bigserial NOT NULL,
    batch_execution_id bigint NOT NULL,
    started_at timestamp NOT NULL,
    ended_at timestamp,
    success_count integer NOT NULL,
    failure_count integer NOT NULL,
    log_file_id bigint,
    CONSTRAINT pk_batch_results PRIMARY KEY (batch_result_id)
);
COMMENT ON TABLE batch_results IS '배치 실행 결과와 성공·실패 건수를 보관한다. 결과 조회와 로그 파일 연결에 사용한다.';
COMMENT ON COLUMN batch_results.batch_result_id IS 'batch_result_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN batch_results.batch_execution_id IS 'batch_execution_id 참조 의도 (FK 미선언)';
COMMENT ON COLUMN batch_results.log_file_id IS 'log_file_id 참조 의도 (FK 미선언)';
CREATE INDEX IF NOT EXISTS ix_user_accounts_status ON user_accounts (status);
CREATE INDEX IF NOT EXISTS ix_user_accounts_enabled ON user_accounts (enabled);
CREATE INDEX IF NOT EXISTS ix_user_accounts_user_id ON user_accounts (user_id);
CREATE INDEX IF NOT EXISTS ix_organizations_enabled ON organizations (enabled);
CREATE INDEX IF NOT EXISTS ix_position_assignments_user_id ON position_assignments (user_id);
CREATE INDEX IF NOT EXISTS ix_roles_enabled ON roles (enabled);
CREATE INDEX IF NOT EXISTS ix_roles_role_code ON roles (role_code);
CREATE INDEX IF NOT EXISTS ix_user_roles_role_code ON user_roles (role_code);
CREATE INDEX IF NOT EXISTS ix_user_roles_user_id ON user_roles (user_id);
CREATE INDEX IF NOT EXISTS ix_menus_menu_id ON menus (menu_id);
CREATE INDEX IF NOT EXISTS ix_menus_screen_id ON menus (screen_id);
CREATE INDEX IF NOT EXISTS ix_menu_permissions_menu_id ON menu_permissions (menu_id);
CREATE INDEX IF NOT EXISTS ix_function_permissions_role_code ON function_permissions (role_code);
CREATE INDEX IF NOT EXISTS ix_function_permissions_screen_id ON function_permissions (screen_id);
CREATE INDEX IF NOT EXISTS ix_data_scope_permissions_role_code ON data_scope_permissions (role_code);
CREATE INDEX IF NOT EXISTS ix_code_groups_enabled ON code_groups (enabled);
CREATE INDEX IF NOT EXISTS ix_system_configurations_enabled ON system_configurations (enabled);
CREATE INDEX IF NOT EXISTS ix_base_years_enabled ON base_years (enabled);
CREATE INDEX IF NOT EXISTS ix_privacy_access_permissions_role_code ON privacy_access_permissions (role_code);
CREATE INDEX IF NOT EXISTS ix_user_sessions_user_id ON user_sessions (user_id);
CREATE INDEX IF NOT EXISTS ix_user_sessions_session_status ON user_sessions (session_status);
CREATE INDEX IF NOT EXISTS ix_audit_logs_log_type ON audit_logs (log_type);
