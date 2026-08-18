package kr.ac.knue.cms.korus;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface KorusSnapshotMapper {
    @Select("""
        select staff_id as "staffId", staff_name as "staffName", organization_code as "organizationCode",
               position_title as "positionTitle", rank_title as "rankTitle", employment_status as "employmentStatus",
               retirement_date as "retirementDate", last_synced_at as "lastSyncedAt"
        from korus_staff_snapshot
        order by staff_id
        """)
    List<KorusStaffSnapshot> listSnapshots();

    @Select("""
        select staff_id as "staffId", staff_name as "staffName", organization_code as "organizationCode",
               position_title as "positionTitle", rank_title as "rankTitle", employment_status as "employmentStatus",
               retirement_date as "retirementDate", last_synced_at as "lastSyncedAt"
        from korus_staff_snapshot
        where staff_id = #{staffId}
        """)
    KorusStaffSnapshot findSnapshot(@Param("staffId") String staffId);
}
