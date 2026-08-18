-- Privacy management vertical slice: field policy catalog, menu, indexes, and seed references.
COMMENT ON COLUMN privacy_field_policies.field_policy_id IS 'privacy_field_policies.field_policy_id 생명주기 식별자';
COMMENT ON COLUMN privacy_field_policies.field_name IS '개인정보 정책 대상 필드명. 실제 사용자 개인정보 값은 저장하지 않음';
COMMENT ON COLUMN privacy_field_policies.privacy_grade IS 'PUBLIC:일반|PERSONAL:개인정보|SENSITIVE:민감정보';
COMMENT ON COLUMN privacy_field_policies.encryption_enabled IS 'PrivacyPolicyManagementService.savePrivacyPolicy 시 AES-256-GCM 암호화 정책 적용 여부로 갱신';
COMMENT ON COLUMN privacy_field_policies.masking_rule IS 'PrivacyPolicyManagementService.savePrivacyPolicy 시 사용자 표시 마스킹 규칙으로 갱신';
COMMENT ON COLUMN privacy_field_policies.log_excluded IS 'PrivacyPolicyManagementService.savePrivacyPolicy 시 감사로그 원문 제외 여부로 갱신';

CREATE UNIQUE INDEX IF NOT EXISTS ux_privacy_field_policies_field_name ON privacy_field_policies (field_name);
CREATE INDEX IF NOT EXISTS ix_privacy_field_policies_privacy_grade ON privacy_field_policies (privacy_grade);
CREATE INDEX IF NOT EXISTS ix_privacy_field_policies_encryption_enabled ON privacy_field_policies (encryption_enabled);
CREATE INDEX IF NOT EXISTS ix_privacy_field_policies_log_excluded ON privacy_field_policies (log_excluded);

INSERT INTO menus (menu_id, parent_menu_id, menu_name, screen_id, url, display_order)
VALUES ('M-PRIVACY', 'M-SECURITY', '개인정보 관리', 'SCR-PRIVACY', '/admin/security/privacy', 70)
ON CONFLICT (menu_id) DO UPDATE SET
    parent_menu_id = EXCLUDED.parent_menu_id,
    menu_name = EXCLUDED.menu_name,
    screen_id = EXCLUDED.screen_id,
    url = EXCLUDED.url,
    display_order = EXCLUDED.display_order;

INSERT INTO menu_permissions (target_type, target_id, menu_id, allowed)
SELECT 'ROLE', role_code, 'M-PRIVACY', role_code = 'R09'
FROM roles
WHERE NOT EXISTS (
    SELECT 1 FROM menu_permissions mp
    WHERE mp.target_type = 'ROLE'
      AND mp.target_id = roles.role_code
      AND mp.menu_id = 'M-PRIVACY'
);

INSERT INTO privacy_field_policies (field_name, privacy_grade, encryption_enabled, masking_rule, log_excluded)
VALUES
    ('person_name', 'PERSONAL', true, '성명 두 번째 글자 마스킹', true),
    ('phone_number', 'PERSONAL', true, '뒤 4자리 마스킹', true),
    ('resident_identifier', 'SENSITIVE', true, '앞 6자리만 표시', true),
    ('researcher_registration_no', 'SENSITIVE', true, '앞 3자리 + 뒤 2자리 표시', true),
    ('bank_account_no', 'SENSITIVE', true, '은행명 + 뒤 4자리 표시', true)
ON CONFLICT (field_name) DO UPDATE SET
    privacy_grade = EXCLUDED.privacy_grade,
    encryption_enabled = EXCLUDED.encryption_enabled,
    masking_rule = EXCLUDED.masking_rule,
    log_excluded = EXCLUDED.log_excluded;
