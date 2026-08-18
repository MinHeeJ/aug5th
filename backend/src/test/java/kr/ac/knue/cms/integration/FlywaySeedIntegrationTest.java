package kr.ac.knue.cms.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class FlywaySeedIntegrationTest {
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    void seed_contains_all_r01_to_r09_roles() {
        List<String> roleCodes = jdbcTemplate.queryForList("select role_code from roles order by role_code", String.class);
        assertThat(roleCodes).containsExactly("R01", "R02", "R03", "R04", "R05", "R06", "R07", "R08", "R09");
    }

    @Test
    void admin_user_has_r09_role_and_can_login_from_seed() {
        Map<String, Object> row = jdbcTemplate.queryForMap("""
            select u.login_id as "loginId", ur.role_code as "roleCode", u.is_system_enabled as "enabled"
            from users u join user_roles ur on ur.user_id = u.user_id
            where u.login_id = 'admin' and ur.role_code = 'R09'
            """);
        assertThat(row.get("loginId")).isEqualTo("admin");
        assertThat(row.get("roleCode")).isEqualTo("R09");
        assertThat(row.get("enabled")).isEqualTo(true);
    }

    @Test
    void seed_contains_nine_sub_menu_permissions_for_r09() {
        Integer count = jdbcTemplate.queryForObject("""
            select count(*) from menu_permissions mp
            join menus m on m.menu_id = mp.menu_id
            where mp.target_type = 'ROLE' and mp.target_id = 'R09' and mp.is_allowed = true and m.menu_level = 'SUB'
            """, Integer.class);
        assertThat(count).isEqualTo(9);
    }

    @Test
    void seed_contains_example_organization_user_and_korus_snapshot() {
        Integer organizationCount = jdbcTemplate.queryForObject("select count(*) from organizations", Integer.class);
        Integer userCount = jdbcTemplate.queryForObject("select count(*) from users", Integer.class);
        Integer snapshotCount = jdbcTemplate.queryForObject("select count(*) from korus_staff_snapshot", Integer.class);
        assertThat(organizationCount).isGreaterThanOrEqualTo(1);
        assertThat(userCount).isGreaterThanOrEqualTo(1);
        assertThat(snapshotCount).isGreaterThanOrEqualTo(1);
    }
}
