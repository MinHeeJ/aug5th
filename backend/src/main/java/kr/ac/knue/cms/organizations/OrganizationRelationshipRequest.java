package kr.ac.knue.cms.organizations;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record OrganizationRelationshipRequest(
    @NotNull UUID organizationId,
    UUID parentOrganizationId,
    @NotNull LocalDate effectiveStartDate,
    LocalDate effectiveEndDate,
    String changeReason
) {
}
