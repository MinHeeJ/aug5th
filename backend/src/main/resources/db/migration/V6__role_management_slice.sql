-- Role management vertical slice seed and query indexes.
COMMENT ON COLUMN roles.role_code IS '역할 관리 화면의 생명주기 식별자이며 저장 CTA에서 변경하지 않는다';
COMMENT ON COLUMN roles.purpose IS '역할 관리 저장 시 역할 목적 설명으로 갱신';
COMMENT ON COLUMN roles.grant_criteria IS '역할 관리 저장 시 부여 기준 설명으로 갱신';
COMMENT ON COLUMN roles.default_data_scope IS 'SELF:본인|DEPARTMENT:소속학과|COLLEGE:단과대학|BUSINESS:담당업무|ALL:전체';
COMMENT ON COLUMN roles.enabled IS '역할 관리 저장 시 로컬 DB에서 사용여부를 갱신';

CREATE INDEX IF NOT EXISTS ix_roles_default_data_scope ON roles (default_data_scope);
CREATE INDEX IF NOT EXISTS ix_roles_role_name ON roles (role_name);

INSERT INTO roles (role_code, role_name, purpose, grant_criteria, default_data_scope, enabled)
VALUES
    ('R01', '교원', '본인 관련 업무를 수행하는 일반 사용자 역할', 'KORUS 교원 신분 사용자에게 기본 부여', 'SELF', true),
    ('R02', '학과장', '소속 학과 교원 관련 업무를 확인하는 역할', '학과장 보직 배정기간이 유효한 사용자에게 부여', 'DEPARTMENT', true),
    ('R03', '단과대학(원) 행정실', '단과대학 또는 대학원 행정 처리 역할', '단과대학 행정 담당자에게 부여', 'COLLEGE', true),
    ('R04', '교수지원과', '기준정보와 평가 관련 행정 관리 역할', '교수지원과 업무담당자에게 부여', 'BUSINESS', true),
    ('R05', '산학협력단', '연구비·간접비·지식재산 관련 자료 관리 역할', '산학협력단 업무담당자에게 부여', 'BUSINESS', true),
    ('R06', '입학인재관리과', '입학·취업률 관련 자료 관리 역할', '입학인재관리과 업무담당자에게 부여', 'BUSINESS', true),
    ('R07', '실적부서', '담당 실적 자료 관리 역할', '실적부서 업무담당자에게 부여', 'BUSINESS', true),
    ('R08', '점수산출 감사자', '산출 과정과 근거를 조회하는 감사 역할', '점수산출 감사자로 지정된 사용자에게 부여', 'ALL', true),
    ('R09', '시스템관리자', '사용자·조직·메뉴·권한·코드·파일·보안·감사·배치 관리를 수행하는 관리자 역할', '시스템 관리자 승인 대상자에게 부여', 'ALL', true)
ON CONFLICT (role_code) DO UPDATE SET
    role_name = EXCLUDED.role_name,
    purpose = EXCLUDED.purpose,
    grant_criteria = EXCLUDED.grant_criteria,
    default_data_scope = EXCLUDED.default_data_scope,
    enabled = EXCLUDED.enabled;
