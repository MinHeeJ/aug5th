package kr.ac.knue.cms.korus;

import java.util.List;
import java.util.Optional;

public interface PersonnelInformationPort {
    List<KorusStaffSnapshot> listSnapshots();
    Optional<KorusStaffSnapshot> findSnapshot(String staffId);
}
