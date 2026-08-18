package kr.ac.knue.cms.users;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserSummary(
    UUID userId,
    String loginId,
    String staffId,
    String staffName,
    String organizationCode,
    String rankTitle,
    String employmentStatus,
    String positionTitle,
    LocalDate retirementDate,
    LocalDateTime lastSyncedAt,
    @JsonProperty("systemEnabled")
    boolean systemEnabled,
    List<String> roles
) {
    @JsonProperty("isSystemEnabled")
    public boolean isSystemEnabled() {
        return systemEnabled;
    }
}
