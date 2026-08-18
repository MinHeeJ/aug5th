package kr.ac.knue.cms.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiOperationStaticCoverageContractTest {
    private static final String ADMIN_USER_ID = "20000000-0000-0000-0000-000000000001";
    private static final String FACULTY_USER_ID = "20000000-0000-0000-0000-000000000002";
    private static final String MENU_ID = "00000000-0000-0000-0000-000000000207";
    private static final String MENU_PARENT_ID = "00000000-0000-0000-0000-000000000130";
    private static final String PERMISSION_MENU_ID = "00000000-0000-0000-0000-000000000205";
    private static final String RELATIONSHIP_ID = "30000000-0000-0000-0000-000000000001";
    private static final String ORGANIZATION_ID = "10000000-0000-0000-0000-000000000002";
    private static final String PARENT_ORGANIZATION_ID = "10000000-0000-0000-0000-000000000001";

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    void classpath_openapi_fixture_is_the_contract_source_for_operation_coverage() throws Exception {
        String yaml = new ClassPathResource("contracts/openapi.yaml").getContentAsString(StandardCharsets.UTF_8);
        assertThat(yaml)
            .contains("/api/admin/users")
            .contains("/api/admin/code-groups/{groupId}/codes")
            .contains("x-required-tests")
            .contains("x-side-effects")
            .contains("x-state-transitions");
    }

    @Test
    void literal_get_operations_return_envelopes_from_backend_to_db_flow() throws Exception {
        Cookie session = loginAsAdmin();

        mvc.perform(get("/api/admin/code-groups").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[*].groupId", hasItem("COMMON")));
        mvc.perform(get("/api/admin/code-groups/COMMON/codes").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", not(nullValue())));
        mvc.perform(get("/api/admin/menu-permissions").param("targetType", "ROLE").param("targetId", "R09").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[*].targetType", hasItem("ROLE")));
        mvc.perform(get("/api/admin/menu-structure").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[*].displayOrder", hasItem(2)));
        mvc.perform(get("/api/admin/menus").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[*].url", hasItem("/admin/menus")));
        mvc.perform(get("/api/admin/roles").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[*].roleCode", hasItem("R09")));
        mvc.perform(get("/api/admin/user-roles").param("userId", FACULTY_USER_ID).cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[*].userId", hasItem(FACULTY_USER_ID)));
        mvc.perform(get("/api/admin/users").param("roleCode", "R09").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[*].loginId", hasItem("admin")));
    }

    @Test
    void auth_login_and_logout_persist_user_sessions_side_effect_and_state_transition() throws Exception {
        Integer beforeActive = jdbcTemplate.queryForObject("select count(*) from user_sessions where status = 'active'", Integer.class);
        MvcResult login = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"admin\"}"))
            .andExpect(status().isOk())
            .andExpect(cookie().httpOnly("SESSION", true))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.userId").value(ADMIN_USER_ID))
            .andReturn();
        Integer afterActive = jdbcTemplate.queryForObject("select count(*) from user_sessions where status = 'active'", Integer.class);
        assertThat(afterActive).isGreaterThan(beforeActive);

        Cookie session = login.getResponse().getCookie("SESSION");
        mvc.perform(post("/api/auth/logout").cookie(session))
            .andExpect(status().isOk())
            .andExpect(cookie().maxAge("SESSION", 0))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("로그아웃되었습니다."));
        Integer loggedOut = jdbcTemplate.queryForObject("select count(*) from user_sessions where status = 'logged_out'", Integer.class);
        assertThat(loggedOut).isGreaterThan(0);
        mvc.perform(post("/api/auth/logout"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void menu_status_update_auth_validation_navigation_and_logical_delete_side_effects_are_observable() throws Exception {
        mvc.perform(patch("/api/admin/menus/00000000-0000-0000-0000-000000000207/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"isUsed\":false,\"changeReason\":\"상태 전이 검증\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

        Cookie session = loginAsAdmin();
        mvc.perform(patch("/api/admin/menus/00000000-0000-0000-0000-000000000207/status")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"changeReason\":\"검증 실패\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fields", hasKey("isUsed")));
        mvc.perform(patch("/api/admin/menus/00000000-0000-0000-0000-000000000207/status")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"isUsed\":false,\"changeReason\":\"비활성 상태 전이\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.isUsed").value(false));
        Map<String, Object> inactive = jdbcTemplate.queryForMap("select is_used, deleted_at from menus where menu_id = ?::uuid", MENU_ID);
        assertThat(inactive.get("is_used")).isEqualTo(false);
        assertThat(inactive.get("deleted_at")).isNull();
        mvc.perform(get("/api/navigation/menus").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[?(@.menuId=='" + MENU_ID + "')]").isEmpty());
        mvc.perform(patch("/api/admin/menus/00000000-0000-0000-0000-000000000207/status")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"isUsed\":true,\"changeReason\":\"활성 상태 전이 복구\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.isUsed").value(true));
    }

    @Test
    void user_system_access_update_requires_auth_and_disables_account_state() throws Exception {
        mvc.perform(patch("/api/admin/users/20000000-0000-0000-0000-000000000002/system-access")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"isSystemEnabled\":false,\"roleCodes\":[\"R01\"],\"changeReason\":\"접근 차단\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));
        Cookie session = loginAsAdmin();
        mvc.perform(patch("/api/admin/users/20000000-0000-0000-0000-000000000002/system-access")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"isSystemEnabled\":false,\"roleCodes\":[\"R01\"],\"changeReason\":\"접근 차단\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.userId").value(FACULTY_USER_ID))
            .andExpect(jsonPath("$.data.systemEnabled").value(false));
        Boolean disabled = jdbcTemplate.queryForObject("select is_system_enabled from users where user_id = ?::uuid", Boolean.class, FACULTY_USER_ID);
        assertThat(disabled).isFalse();
        mvc.perform(patch("/api/admin/users/20000000-0000-0000-0000-000000000002/system-access")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"isSystemEnabled\":true,\"roleCodes\":[\"R01\"],\"changeReason\":\"접근 복구\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.systemEnabled").value(true));
    }

    @Test
    void code_group_and_code_updates_validate_business_fields_and_persist_side_effects() throws Exception {
        Cookie session = loginAsAdmin();
        mvc.perform(put("/api/admin/code-groups/COMMON")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupId\":\"COMMON\",\"groupName\":\"공통\",\"description\":\"공통 코드 계약 검증\",\"managingDepartment\":\"시스템관리부서\",\"isUsed\":true}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.groupId").value("COMMON"))
            .andExpect(jsonPath("$.data.description").value("공통 코드 계약 검증"));
        assertThat(jdbcTemplate.queryForObject("select description from code_groups where group_id = 'COMMON'", String.class))
            .isEqualTo("공통 코드 계약 검증");

        mvc.perform(put("/api/admin/code-groups/COMMON/codes/ACTIVE")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupId\":\"COMMON\",\"codeValue\":\"ACTIVE\",\"codeName\":\"활성\",\"sortOrder\":1,\"isUsed\":true}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.codeValue").value("ACTIVE"))
            .andExpect(jsonPath("$.data.parentCodeId").isEmpty())
            .andExpect(jsonPath("$.data.isUsed").value(true));
        mvc.perform(put("/api/admin/code-groups/COMMON/codes/ACTIVE")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"groupId\":\"COMMON\",\"codeValue\":\"ACTIVE\",\"codeName\":\"활성\",\"sortOrder\":1,\"isUsed\":false}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.isUsed").value(false));
        Map<String, Object> codeRow = jdbcTemplate.queryForMap("select is_used, parent_code_id from codes where group_id = 'COMMON' and code_value = 'ACTIVE'");
        assertThat(codeRow.get("is_used")).isEqualTo(false);
        assertThat(codeRow.get("parent_code_id")).isNull();
    }

    @Test
    void menu_permission_update_and_structure_updates_persist_business_side_effects() throws Exception {
        Cookie session = loginAsAdmin();
        mvc.perform(put("/api/admin/menu-permissions/ROLE/R09")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"permissions\":[{\"menuId\":\"00000000-0000-0000-0000-000000000205\",\"isAllowed\":true}],\"changeReason\":\"권한 허용\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[*].menuId", hasItem(PERMISSION_MENU_ID)))
            .andExpect(jsonPath("$.data[?(@.menuId=='" + PERMISSION_MENU_ID + "')].isAllowed", hasItem(true)));
        Boolean allowed = jdbcTemplate.queryForObject("select is_allowed from menu_permissions where target_type = 'ROLE' and target_id = 'R09' and menu_id = ?::uuid", Boolean.class, PERMISSION_MENU_ID);
        assertThat(allowed).isTrue();

        mvc.perform(put("/api/admin/menu-structure/00000000-0000-0000-0000-000000000207")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"menuId\":\"00000000-0000-0000-0000-000000000207\",\"parentMenuId\":\"00000000-0000-0000-0000-000000000130\",\"menuLevel\":\"SUB\",\"displayOrder\":3,\"menuName\":\"메뉴 정보 관리\",\"screenId\":\"SCR-MENU-INFO\",\"url\":\"/admin/menus\",\"icon\":\"layout\",\"businessDivision\":\"공통\",\"description\":\"구조 계약\",\"isUsed\":true,\"changeReason\":\"순서 변경\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.displayOrder").value(3));
        mvc.perform(put("/api/admin/menu-structure/reorder")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"parentMenuId\":\"00000000-0000-0000-0000-000000000130\",\"orderedMenuIds\":[\"00000000-0000-0000-0000-000000000206\",\"00000000-0000-0000-0000-000000000207\"]}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[*].menuId", hasItem(MENU_ID)));
        Integer displayOrder = jdbcTemplate.queryForObject("select display_order from menus where menu_id = ?::uuid", Integer.class, MENU_ID);
        assertThat(displayOrder).isEqualTo(2);
    }

    @Test
    void menu_information_role_relationship_and_user_role_updates_persist_required_side_effects() throws Exception {
        Cookie session = loginAsAdmin();
        mvc.perform(put("/api/admin/menus/00000000-0000-0000-0000-000000000207")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"menuId\":\"00000000-0000-0000-0000-000000000207\",\"parentMenuId\":\"00000000-0000-0000-0000-000000000130\",\"menuLevel\":\"SUB\",\"displayOrder\":2,\"menuName\":\"메뉴 정보 관리\",\"screenId\":\"SCR-MENU-INFO\",\"url\":\"/admin/menus\",\"icon\":\"layout\",\"businessDivision\":\"공통\",\"description\":\"navigation 연결 검증\",\"isUsed\":true,\"changeReason\":\"정보 변경\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.url").value("/admin/menus"));
        assertThat(jdbcTemplate.queryForObject("select url from menus where menu_id = ?::uuid", String.class, MENU_ID))
            .isEqualTo("/api/admin/menus");

        mvc.perform(put("/api/admin/roles/R03")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roleCode\":\"R03\",\"roleName\":\"학과 관리자\",\"rolePurpose\":\"학과 단위 관리\",\"assignmentCriteria\":\"보직\",\"defaultDataScope\":\"DEPARTMENT\",\"isUsed\":true}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.roleCode").value("R03"))
            .andExpect(jsonPath("$.data.defaultDataScope").value("DEPARTMENT"));
        assertThat(jdbcTemplate.queryForObject("select default_data_scope from roles where role_code = 'R03'", String.class))
            .isEqualTo("DEPARTMENT");

        mvc.perform(put("/api/admin/organization-relationships/30000000-0000-0000-0000-000000000001")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"organizationId\":\"10000000-0000-0000-0000-000000000002\",\"parentOrganizationId\":\"10000000-0000-0000-0000-000000000001\",\"effectiveStartDate\":\"2026-01-01\",\"changeReason\":\"계층 변경 이력\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.relationshipId").value(RELATIONSHIP_ID));
        Map<String, Object> relationship = jdbcTemplate.queryForMap("select organization_id, parent_organization_id, change_reason from organization_relationship_history where relationship_id = ?::uuid", RELATIONSHIP_ID);
        assertThat(String.valueOf(relationship.get("organization_id"))).isEqualTo(ORGANIZATION_ID);
        assertThat(String.valueOf(relationship.get("parent_organization_id"))).isEqualTo(PARENT_ORGANIZATION_ID);

        mvc.perform(put("/api/admin/user-roles/20000000-0000-0000-0000-000000000002")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roles\":[{\"roleCode\":\"R03\",\"assignmentType\":\"MANUAL\",\"validFrom\":\"2026-01-01\",\"validTo\":\"2026-12-31\",\"approvedByUserId\":\"20000000-0000-0000-0000-000000000001\",\"isUsed\":true}]}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[*].roleCode", hasItem("R03")))
            .andExpect(jsonPath("$.data[?(@.roleCode=='R03')].isUsed", hasItem(true)));
        mvc.perform(put("/api/admin/user-roles/20000000-0000-0000-0000-000000000002")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roles\":[{\"roleCode\":\"R03\",\"assignmentType\":\"MANUAL\",\"validFrom\":\"2026-01-01\",\"approvedByUserId\":\"20000000-0000-0000-0000-000000000001\",\"isUsed\":false}]}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[?(@.roleCode=='R03')].isUsed", hasItem(false)))
            .andExpect(jsonPath("$.data[?(@.roleCode=='R03')].revokedAt").exists());
        Integer revoked = jdbcTemplate.queryForObject("select count(*) from user_roles where user_id = ?::uuid and role_code = 'R03' and is_used = false and revoked_at is not null", Integer.class, FACULTY_USER_ID);
        assertThat(revoked).isGreaterThan(0);
    }

    @Test
    void write_operations_reject_validation_or_missing_business_targets() throws Exception {
        Cookie session = loginAsAdmin();
        mvc.perform(put("/api/admin/user-roles/20000000-0000-0000-0000-000000000002")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roles\":[]}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fields", hasKey("roles")));
        mvc.perform(put("/api/admin/roles/R03")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roleCode\":\"R03\",\"roleName\":\"\",\"rolePurpose\":\"목적\",\"assignmentCriteria\":\"기준\",\"defaultDataScope\":\"DEPARTMENT\",\"isUsed\":true}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fields", hasKey("roleName")));
        mvc.perform(put("/api/admin/menu-permissions/ROLE/NO_ROLE")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"permissions\":[{\"menuId\":\"00000000-0000-0000-0000-000000000205\",\"isAllowed\":true}],\"changeReason\":\"대상 없음\"}"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false));
    }

    private Cookie loginAsAdmin() throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"admin\"}"))
            .andExpect(status().isOk())
            .andExpect(cookie().exists("SESSION"))
            .andReturn();
        return result.getResponse().getCookie("SESSION");
    }
}
