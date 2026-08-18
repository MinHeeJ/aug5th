package kr.ac.knue.cms.organizations;

import java.time.LocalDate;
import java.util.UUID;

public record OrganizationRelationshipDto(
    UUID relationshipId,
    UUID organizationId,
    UUID parentOrganizationId,
    LocalDate effectiveStartDate,
    LocalDate effectiveEndDate,
    String changeReason
) {
}
