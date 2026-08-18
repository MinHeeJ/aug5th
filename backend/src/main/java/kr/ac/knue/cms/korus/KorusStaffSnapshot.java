package kr.ac.knue.cms.korus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record KorusStaffSnapshot(String staffId, String staffName, String organizationCode, String positionTitle,
                                 String rankTitle, String employmentStatus, LocalDate retirementDate,
                                 LocalDateTime lastSyncedAt) {
}
