CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS organizations (
    organization_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_code varchar(50) NOT NULL UNIQUE,
    organization_name varchar(200) NOT NULL,
    organization_type varchar(30) NOT NULL CHECK (organization_type IN ('UNIVERSITY','GRADUATE_SCHOOL','COLLEGE','DEPARTMENT','ADMIN_DEPARTMENT')),
    is_used boolean NOT NULL DEFAULT true,
    deleted_at timestamp NULL,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp NOT NULL DEFAULT now()
);
COMMENT ON TABLE organizations IS '대학·대학원·단과대학·학과·부서 조직 기준정보를 조직코드로 관리한다.';
COMMENT ON COLUMN organizations.organization_type IS 'UNIVERSITY:대학교|GRADUATE_SCHOOL:대학원|COLLEGE:단과대학|DEPARTMENT:학과|ADMIN_DEPARTMENT:행정부서';

CREATE TABLE IF NOT EXISTS korus_staff_snapshot (
    staff_id varchar(50) PRIMARY KEY,
    staff_name varchar(100) NOT NULL,
    organization_code varchar(50) NULL REFERENCES organizations(organization_code),
    position_title varchar(100) NULL,
    rank_title varchar(100) NULL,
    employment_status varchar(30) NOT NULL,
    retirement_date date NULL,
    last_synced_at timestamp NOT NULL,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp NOT NULL DEFAULT now()
);
COMMENT ON TABLE korus_staff_snapshot IS 'KORUS 원천 인사·조직 정보를 로컬 Mock snapshot으로 제공하며 애플리케이션에서 조회 전용으로 사용한다.';
COMMENT ON COLUMN korus_staff_snapshot.employment_status IS 'ACTIVE:재직|RETIRED:퇴직|LEAVE:휴직';

CREATE TABLE IF NOT EXISTS users (
    user_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    login_id varchar(100) NOT NULL UNIQUE,
    password_hash varchar(255) NOT NULL,
    korus_staff_id varchar(50) NULL REFERENCES korus_staff_snapshot(staff_id),
    is_system_enabled boolean NOT NULL DEFAULT true,
    status varchar(30) NOT NULL DEFAULT 'ACTIVE',
    deleted_at timestamp NULL,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp NOT NULL DEFAULT now()
);
COMMENT ON TABLE users IS '내부 사용자 계정과 시스템 사용여부를 로컬 DB에서 관리한다.';
COMMENT ON COLUMN users.status IS 'ACTIVE:활성|DISABLED:비활성|DELETED:삭제';

CREATE TABLE IF NOT EXISTS organization_user_mappings (
    mapping_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id uuid NOT NULL REFERENCES organizations(organization_id),
    user_id uuid NOT NULL REFERENCES users(user_id),
    position_title varchar(100) NULL,
    valid_from date NULL,
    valid_to date NULL,
    is_used boolean NOT NULL DEFAULT true,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp NOT NULL DEFAULT now(),
    CONSTRAINT organization_user_mappings_period_check CHECK (valid_to IS NULL OR valid_from IS NULL OR valid_from <= valid_to)
);
COMMENT ON TABLE organization_user_mappings IS '보직 또는 조직과 사용자를 연결해 조직별 사용자 기준을 관리한다.';

CREATE TABLE IF NOT EXISTS organization_relationship_history (
    relationship_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id uuid NOT NULL REFERENCES organizations(organization_id),
    parent_organization_id uuid NULL REFERENCES organizations(organization_id),
    effective_start_date date NOT NULL,
    effective_end_date date NULL,
    changed_by_user_id uuid NULL REFERENCES users(user_id),
    changed_at timestamp NOT NULL DEFAULT now(),
    change_reason varchar(500) NULL,
    before_value text NULL,
    after_value text NULL,
    is_used boolean NOT NULL DEFAULT true,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp NOT NULL DEFAULT now(),
    CONSTRAINT organization_relationship_period_check CHECK (effective_end_date IS NULL OR effective_start_date <= effective_end_date)
);
COMMENT ON TABLE organization_relationship_history IS '조직 상위관계와 적용기간 변경 이력을 업무 이력으로 보존한다.';
COMMENT ON COLUMN organization_relationship_history.changed_by_user_id IS 'users.user_id 참조 의도 (처리자)';
COMMENT ON COLUMN organization_relationship_history.before_value IS '변경 전 상위조직/기간 값을 애플리케이션 저장 시 기록';
COMMENT ON COLUMN organization_relationship_history.after_value IS '변경 후 상위조직/기간 값을 애플리케이션 저장 시 기록';

CREATE TABLE IF NOT EXISTS roles (
    role_code varchar(3) PRIMARY KEY,
    role_name varchar(100) NOT NULL,
    role_purpose varchar(500) NULL,
    assignment_criteria varchar(1000) NULL,
    default_data_scope varchar(500) NULL,
    is_used boolean NOT NULL DEFAULT true,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp NOT NULL DEFAULT now(),
    CONSTRAINT roles_code_check CHECK (role_code IN ('R01','R02','R03','R04','R05','R06','R07','R08','R09'))
);
COMMENT ON TABLE roles IS 'R01~R09 업무 역할 기준정보와 목적·부여기준·기본 데이터 범위를 관리한다.';

CREATE TABLE IF NOT EXISTS user_roles (
    user_role_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users(user_id),
    role_code varchar(3) NOT NULL REFERENCES roles(role_code),
    assignment_type varchar(20) NOT NULL CHECK (assignment_type IN ('POSITION','MANUAL')),
    valid_from date NULL,
    valid_to date NULL,
    approved_by_user_id uuid NULL REFERENCES users(user_id),
    revoked_at timestamp NULL,
    before_value text NULL,
    after_value text NULL,
    change_reason varchar(500) NULL,
    is_used boolean NOT NULL DEFAULT true,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp NOT NULL DEFAULT now(),
    CONSTRAINT user_roles_period_check CHECK (valid_to IS NULL OR valid_from IS NULL OR valid_from <= valid_to)
);
COMMENT ON TABLE user_roles IS '사용자별 역할 부여·변경·회수와 유효기간·승인자를 보존한다.';
COMMENT ON COLUMN user_roles.assignment_type IS 'POSITION:보직기반|MANUAL:수동';
COMMENT ON COLUMN user_roles.approved_by_user_id IS 'users.user_id 참조 의도 (승인자)';
COMMENT ON COLUMN user_roles.before_value IS '역할 변경 전 값을 애플리케이션 저장 시 기록';
COMMENT ON COLUMN user_roles.after_value IS '역할 변경 후 값을 애플리케이션 저장 시 기록';

CREATE TABLE IF NOT EXISTS menus (
    menu_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_menu_id uuid NULL REFERENCES menus(menu_id),
    menu_level varchar(20) NOT NULL CHECK (menu_level IN ('MAIN','MIDDLE','SUB')),
    display_order integer NOT NULL,
    menu_name varchar(200) NOT NULL,
    screen_id varchar(100) NULL,
    url varchar(300) NULL,
    icon varchar(100) NULL,
    business_division varchar(100) NULL,
    description varchar(1000) NULL,
    is_used boolean NOT NULL DEFAULT true,
    deleted_at timestamp NULL,
    before_value text NULL,
    after_value text NULL,
    change_reason varchar(500) NULL,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp NOT NULL DEFAULT now()
);
COMMENT ON TABLE menus IS '대·중·소 메뉴 구조와 실행 화면 연결 정보를 관리한다.';
COMMENT ON COLUMN menus.menu_level IS 'MAIN:대메뉴|MIDDLE:중메뉴|SUB:소메뉴';
COMMENT ON COLUMN menus.before_value IS '메뉴 구조/실행정보 변경 전 값을 애플리케이션 저장 시 기록';
COMMENT ON COLUMN menus.after_value IS '메뉴 구조/실행정보 변경 후 값을 애플리케이션 저장 시 기록';

CREATE TABLE IF NOT EXISTS menu_permissions (
    permission_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    target_type varchar(20) NOT NULL CHECK (target_type IN ('ROLE','ORGANIZATION','USER')),
    target_id varchar(100) NOT NULL,
    menu_id uuid NOT NULL REFERENCES menus(menu_id),
    is_allowed boolean NOT NULL DEFAULT true,
    before_value text NULL,
    after_value text NULL,
    change_reason varchar(500) NULL,
    is_used boolean NOT NULL DEFAULT true,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp NOT NULL DEFAULT now(),
    CONSTRAINT menu_permissions_unique_target_menu UNIQUE (target_type, target_id, menu_id)
);
COMMENT ON TABLE menu_permissions IS '역할·조직·사용자 단위 메뉴 접근 허용 여부를 관리한다.';
COMMENT ON COLUMN menu_permissions.target_type IS 'ROLE:역할|ORGANIZATION:조직|USER:사용자';
COMMENT ON COLUMN menu_permissions.target_id IS 'roles.role_code 또는 organizations.organization_id 또는 users.user_id 참조 의도';
COMMENT ON COLUMN menu_permissions.before_value IS '권한 변경 전 값을 애플리케이션 저장 시 기록';
COMMENT ON COLUMN menu_permissions.after_value IS '권한 변경 후 값을 애플리케이션 저장 시 기록';

CREATE TABLE IF NOT EXISTS code_groups (
    group_id varchar(100) PRIMARY KEY,
    group_name varchar(200) NOT NULL,
    description varchar(1000) NULL,
    managing_department varchar(200) NULL,
    is_used boolean NOT NULL DEFAULT true,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp NOT NULL DEFAULT now()
);
COMMENT ON TABLE code_groups IS '공통코드 상세코드의 상위 그룹 기준정보를 관리한다.';

CREATE TABLE IF NOT EXISTS codes (
    code_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id varchar(100) NOT NULL REFERENCES code_groups(group_id),
    code_value varchar(100) NOT NULL,
    code_name varchar(200) NOT NULL,
    parent_code_id uuid NULL REFERENCES codes(code_id),
    sort_order integer NOT NULL DEFAULT 0,
    extra_attributes jsonb NULL,
    valid_from date NULL,
    valid_to date NULL,
    is_used boolean NOT NULL DEFAULT true,
    deleted_at timestamp NULL,
    before_value text NULL,
    after_value text NULL,
    change_reason varchar(500) NULL,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp NOT NULL DEFAULT now(),
    CONSTRAINT codes_unique_group_value UNIQUE (group_id, code_value),
    CONSTRAINT codes_period_check CHECK (valid_to IS NULL OR valid_from IS NULL OR valid_from <= valid_to)
);
COMMENT ON TABLE codes IS '코드그룹별 상세코드, 계층, 연계 속성, 유효기간과 사용여부를 관리한다.';
COMMENT ON COLUMN codes.extra_attributes IS '상세코드 저장 API가 연계 코드 매핑용 속성을 저장할 때 갱신';
COMMENT ON COLUMN codes.before_value IS '상세코드 변경 전 값을 애플리케이션 저장 시 기록';
COMMENT ON COLUMN codes.after_value IS '상세코드 변경 후 값을 애플리케이션 저장 시 기록';

CREATE TABLE IF NOT EXISTS user_sessions (
    session_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES users(user_id),
    session_token_hash varchar(255) NOT NULL UNIQUE,
    expires_at timestamp NOT NULL,
    status varchar(30) NOT NULL,
    is_used boolean NOT NULL DEFAULT true,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp NOT NULL DEFAULT now()
);
COMMENT ON TABLE user_sessions IS '내부 계정 인증 세션과 로그아웃·만료 상태를 관리한다.';
COMMENT ON COLUMN user_sessions.status IS 'active:활성|logged_out:로그아웃|expired:만료';

CREATE INDEX IF NOT EXISTS idx_users_login_id ON users(login_id);
CREATE INDEX IF NOT EXISTS idx_users_korus_staff_id ON users(korus_staff_id);
CREATE INDEX IF NOT EXISTS idx_korus_staff_organization_code ON korus_staff_snapshot(organization_code);
CREATE INDEX IF NOT EXISTS idx_organization_relationship_current ON organization_relationship_history(organization_id, effective_start_date, effective_end_date);
CREATE INDEX IF NOT EXISTS idx_user_roles_user_role ON user_roles(user_id, role_code);
CREATE INDEX IF NOT EXISTS idx_menus_parent_order ON menus(parent_menu_id, display_order);
CREATE INDEX IF NOT EXISTS idx_menu_permissions_target ON menu_permissions(target_type, target_id);
CREATE INDEX IF NOT EXISTS idx_codes_group_sort ON codes(group_id, sort_order);
CREATE INDEX IF NOT EXISTS idx_user_sessions_token_hash ON user_sessions(session_token_hash);
