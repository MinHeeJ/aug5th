package kr.ac.knue.cms.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import kr.ac.knue.cms.korus.KorusStaffSnapshot;
import kr.ac.knue.cms.korus.PersonnelInformationPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class KorusReadOnlyIntegrationTest {
    @Autowired PersonnelInformationPort personnelInformationPort;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired MockMvc mockMvc;

    @Test
    void personnel_information_port_exposes_korus_snapshot_read_only_data() {
        KorusStaffSnapshot snapshot = personnelInformationPort.findSnapshot("STAFF-001").orElseThrow();
        assertThat(snapshot.staffName()).isEqualTo("홍길동");
        assertThat(snapshot.organizationCode()).isEqualTo("EDU-COL");
    }

    @Test
    void no_korus_mutation_endpoint_is_exposed() throws Exception {
        mockMvc.perform(patch("/api/korus/staff/STAFF-001").content("""
                    {"staffName":"변경"}
                    """))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void local_users_table_is_separate_from_korus_snapshot_source_table() {
        Integer linkedUsers = jdbcTemplate.queryForObject("""
            select count(*) from users u join korus_staff_snapshot k on k.staff_id = u.korus_staff_id
            where u.login_id = 'faculty' and k.staff_id = 'STAFF-001'
            """, Integer.class);
        assertThat(linkedUsers).isEqualTo(1);
    }
}
