package kr.ac.knue.commonfoundation.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class FoundationMigrationContractTest {

    private static final List<String> ENTITY_TABLES = List.of(
        "user_accounts",
        "korus_personnel_snapshots",
        "organizations",
        "position_assignments",
        "roles",
        "user_roles",
        "menus",
        "menu_permissions",
        "function_permissions",
        "data_scope_permissions",
        "code_groups",
        "code_details",
        "system_configurations",
        "base_years",
        "file_policies",
        "notices",
        "attachment_files",
        "excel_templates",
        "excel_upload_histories",
        "excel_upload_errors",
        "excel_download_requests",
        "privacy_field_policies",
        "privacy_access_permissions",
        "privacy_access_histories",
        "user_sessions",
        "session_termination_histories",
        "audit_logs",
        "batch_definitions",
        "batch_executions",
        "batch_results"
    );

    @Test
    void flywaySchemaCreatesEveryDataModelEntityTableIdempotently() throws IOException {
        String migration = migration("db/migration/V1__foundation_schema.sql");

        for (String table : ENTITY_TABLES) {
            assertThat(migration)
                .as(table + " must be created by the foundation Flyway migration")
                .contains("CREATE TABLE IF NOT EXISTS " + table);
            assertThat(migration)
                .as(table + " must document its business role")
                .contains("COMMENT ON TABLE " + table + " IS");
        }
        assertThat(migration).contains("CREATE INDEX IF NOT EXISTS");
        assertThat(migration).contains("COMMENT ON COLUMN user_accounts.status IS 'ACTIVE:사용|INACTIVE:미사용|LOCKED:잠김'");
        assertThat(migration).contains("COMMENT ON COLUMN data_scope_permissions.scope_type IS 'SELF:본인|DEPARTMENT:소속학과|COLLEGE:단과대학|BUSINESS:담당업무|ALL:전체'");
    }

    @Test
    void seedMigrationProvidesAdminRolesMenusAndFunctionPermissions() throws IOException {
        String seed = migration("db/migration/V2__foundation_seed.sql");

        assertThat(seed).contains("'admin', true, 'R09 시스템관리자'");
        for (int roleNumber = 1; roleNumber <= 9; roleNumber++) {
            assertThat(seed).contains("'R0" + roleNumber + "'");
        }
        assertThat(seed).contains("SCR-USER-MGMT");
        assertThat(seed).contains("SCR-BATCH-RESULT");
        assertThat(seed).contains("INSERT INTO menu_permissions");
        assertThat(seed).contains("INSERT INTO function_permissions");
        assertThat(seed).contains("INSERT INTO data_scope_permissions");
    }

    private static String migration(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        assertThat(resource.exists()).as(path + " must exist").isTrue();
        return resource.getContentAsString(StandardCharsets.UTF_8);
    }
}
