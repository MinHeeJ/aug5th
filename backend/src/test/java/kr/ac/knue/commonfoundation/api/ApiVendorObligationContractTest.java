package kr.ac.knue.commonfoundation.api;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class ApiVendorObligationContractTest {

    @Test
    void openApiPostOperationsDeclareRequiredTestsSideEffectsAndStateTransitions() throws Exception {
        String contract = new String(new ClassPathResource("contracts/openapi.yaml").getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        assertOperationVendorObligation(contract, "/api/admin/attachments", "post:", "x-required-tests:", "auth", "business", "happy", "side-effect", "validation", "x-side-effects:", "attachment_files", "audit_logs");
        assertOperationVendorObligation(contract, "/api/admin/attachments/integrity-checks", "post:", "x-required-tests:", "auth", "business", "happy", "side-effect", "validation");
        assertOperationVendorObligation(contract, "/api/admin/attachments/{attachmentId}/delete", "post:", "x-required-tests:", "auth", "business", "happy", "side-effect", "validation");
        assertOperationVendorObligation(contract, "/api/admin/audit-logs", "post:", "x-required-tests:", "auth", "business", "happy", "side-effect", "validation", "x-side-effects:", "audit_logs");
        assertOperationVendorObligation(contract, "/api/admin/base-years", "post:", "x-required-tests:", "auth", "business", "happy", "side-effect", "validation", "x-side-effects:", "audit_logs", "base_years");
        assertOperationVendorObligation(contract, "/api/admin/batch-definitions", "post:", "x-required-tests:", "auth", "business", "happy", "side-effect", "validation", "x-side-effects:", "audit_logs", "batch_definitions");
        assertOperationVendorObligation(contract, "/api/admin/batch-executions", "post:", "x-required-tests:", "auth", "business", "happy", "side-effect", "validation", "x-side-effects:", "audit_logs", "batch_executions");
        assertOperationVendorObligation(contract, "/api/admin/batch-executions/{executionId}/rerun", "post:", "x-required-tests:", "auth", "business", "happy", "side-effect", "validation");
        assertOperationVendorObligation(contract, "/api/admin/batch-executions/{executionId}/stop", "post:", "x-required-tests:", "auth", "business", "happy", "side-effect", "validation");
        assertOperationVendorObligation(contract, "/api/admin/batch-results", "post:", "x-required-tests:", "auth", "business", "happy", "side-effect", "validation", "x-side-effects:", "audit_logs", "batch_results");
        assertOperationVendorObligation(contract, "/api/admin/code-details", "post:", "x-required-tests:", "auth", "business", "happy", "side-effect", "validation", "x-side-effects:", "audit_logs", "code_details");
        assertOperationVendorObligation(contract, "/api/admin/code-groups", "post:", "x-required-tests:", "auth", "business", "happy", "side-effect", "validation", "x-side-effects:", "audit_logs", "code_groups");
        assertOperationVendorObligation(contract, "/api/admin/data-scopes", "post:", "x-required-tests:", "auth", "business", "happy", "side-effect", "validation", "x-side-effects:", "audit_logs", "data_scope_permissions");
        assertOperationVendorObligation(contract, "/api/admin/excel-downloads", "post:", "x-required-tests:", "auth", "business", "happy", "side-effect", "validation", "x-side-effects:", "audit_logs", "excel_download_requests");
        assertOperationVendorObligation(contract, "/api/admin/excel-templates", "post:", "x-required-tests:", "auth", "business", "happy", "side-effect", "validation", "x-side-effects:", "audit_logs", "excel_templates");
        assertOperationVendorObligation(contract, "/api/admin/excel-uploads", "post:", "x-required-tests:", "auth", "business", "happy", "side-effect", "validation", "x-side-effects:", "audit_logs", "excel_upload_histories");
        assertOperationVendorObligation(contract, "/api/admin/file-policies", "post:", "x-required-tests:", "auth", "business", "happy", "side-effect", "validation", "x-side-effects:", "audit_logs", "file_policies");
        assertOperationVendorObligation(contract, "/api/admin/function-permissions", "post:", "x-required-tests:", "auth", "business", "happy", "side-effect", "validation", "x-side-effects:", "audit_logs", "function_permissions");
        assertOperationVendorObligation(contract, "/api/admin/menu-permissions", "post:", "x-required-tests:", "auth", "business", "happy", "side-effect", "validation", "x-side-effects:", "audit_logs", "menu_permissions");
        assertOperationVendorObligation(contract, "/api/admin/menus", "post:", "x-required-tests:", "auth", "business", "happy", "side-effect", "validation", "x-side-effects:", "audit_logs");
        assertOperationVendorObligation(contract, "/api/admin/notices", "post:", "x-required-tests:", "auth", "business", "happy", "side-effect", "validation", "x-side-effects:", "audit_logs");
        assertOperationVendorObligation(contract, "/api/admin/organizations", "post:", "x-required-tests:", "auth", "business", "happy", "side-effect", "validation", "x-side-effects:", "audit_logs");
        assertOperationVendorObligation(contract, "/api/admin/positions", "post:", "x-required-tests:", "auth", "business", "happy", "side-effect", "validation", "x-side-effects:", "audit_logs", "position_assignments");
        assertOperationVendorObligation(contract, "/api/admin/privacy-policies", "post:", "x-required-tests:", "auth", "business", "happy", "side-effect", "validation", "x-side-effects:", "audit_logs", "privacy_field_policies");
        assertOperationVendorObligation(contract, "/api/admin/roles", "post:", "x-required-tests:", "auth", "business", "happy", "side-effect", "validation", "x-side-effects:", "audit_logs");
        assertOperationVendorObligation(contract, "/api/admin/sessions", "post:", "x-required-tests:", "auth", "business", "happy", "side-effect", "validation", "x-side-effects:", "audit_logs", "user_sessions");
        assertOperationVendorObligation(contract, "/api/admin/sessions/{sessionId}/terminate", "post:", "x-required-tests:", "auth", "business", "happy", "side-effect", "validation");
        assertOperationVendorObligation(contract, "/api/admin/system-configurations", "post:", "x-required-tests:", "auth", "business", "happy", "side-effect", "validation", "x-side-effects:", "audit_logs", "system_configurations");
        assertOperationVendorObligation(contract, "/api/admin/user-roles", "post:", "x-required-tests:", "auth", "business", "happy", "side-effect", "validation", "x-side-effects:", "audit_logs", "user_roles");
        assertOperationVendorObligation(contract, "/api/admin/users", "post:", "x-required-tests:", "auth", "business", "happy", "side-effect", "validation", "x-side-effects:", "audit_logs", "user_accounts");
        assertOperationVendorObligation(contract, "/api/auth/login", "post:", "x-required-tests:", "auth", "business", "happy", "side-effect", "validation", "x-side-effects:", "active", "user_sessions");
        assertOperationVendorObligation(contract, "/api/auth/logout", "post:", "x-required-tests:", "auth", "business", "happy", "side-effect", "validation", "x-side-effects:", "session_termination_histories", "user_sessions", "x-state-transitions:");
    }

    private static void assertOperationVendorObligation(String contract, String path, String method, String... requiredSignals) {
        int pathIndex = contract.indexOf(path + ":");
        org.assertj.core.api.Assertions.assertThat(pathIndex).as(path).isGreaterThanOrEqualTo(0);
        int methodIndex = contract.indexOf(method, pathIndex);
        org.assertj.core.api.Assertions.assertThat(methodIndex).as(path + " " + method).isGreaterThanOrEqualTo(pathIndex);
        int nextPathIndex = contract.indexOf("\n  /api/", pathIndex + 1);
        String operationBlock = contract.substring(methodIndex, nextPathIndex == -1 ? contract.length() : nextPathIndex);
        for (String requiredSignal : requiredSignals) {
            org.assertj.core.api.Assertions.assertThat(operationBlock).as(path + " requires " + requiredSignal).contains(requiredSignal);
        }
    }
}
