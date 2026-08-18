package kr.ac.knue.cms.korus;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class MockSnapshotPersonnelInformationAdapter implements PersonnelInformationPort {
    private final KorusSnapshotMapper mapper;

    public MockSnapshotPersonnelInformationAdapter(KorusSnapshotMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<KorusStaffSnapshot> listSnapshots() {
        return mapper.listSnapshots();
    }

    @Override
    public Optional<KorusStaffSnapshot> findSnapshot(String staffId) {
        return Optional.ofNullable(mapper.findSnapshot(staffId));
    }
}
